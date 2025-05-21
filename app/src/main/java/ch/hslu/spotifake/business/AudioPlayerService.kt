package ch.hslu.spotifake.business

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import ch.hslu.spotifake.MainActivity
import ch.hslu.spotifake.db.PlaylistDao
import ch.hslu.spotifake.db.Track
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : MediaBrowserServiceCompat() {

    companion object {
        private const val CHANNEL_ID = "audio_playback_channel"
        private const val NOTIF_ID = 1

        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PLAY_TRACKS = "ACTION_PLAY_TRACKS"

        const val EXTRA_TRACK_IDS = "EXTRA_TRACK_IDS"
        const val EXTRA_START_INDEX = "EXTRA_START_INDEX"
    }

    @Inject lateinit var playlistDao: PlaylistDao
    @Inject lateinit var playbackRepo: PlaybackRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var trackList: List<Track> = emptyList()
    private var currentIndex = 0

    private lateinit var mediaSession: MediaSessionCompat
    private val playbackStateBuilder = PlaybackStateCompat.Builder()
    private var mediaPlayer: MediaPlayer? = null

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
        sessionToken = mediaSession.sessionToken

        serviceScope.launch {
            playlistDao.getAllTracks().collect { tracks ->
                trackList = tracks
                // Optional: reset index if list became empty or changed
                if (currentIndex >= trackList.size) {
                    currentIndex = 0
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY_PAUSE -> {
                    val state = mediaSession.controller.playbackState?.state
                    if (state == PlaybackStateCompat.STATE_PLAYING) {
                        mediaSession.controller.transportControls.pause()
                    } else {
                        mediaSession.controller.transportControls.play()
                    }
                }
                ACTION_NEXT -> mediaSession.controller.transportControls.skipToNext()
                ACTION_PREV -> mediaSession.controller.transportControls.skipToPrevious()
                ACTION_STOP -> mediaSession.controller.transportControls.stop()
                ACTION_PLAY_TRACKS -> {
                    val playlistId = intent.getIntArrayExtra(EXTRA_TRACK_IDS)
                    val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)
                    if (playlistId?.isNotEmpty() == true) {
                        serviceScope.launch {
                            trackList = playlistDao.loadAllTracksByIds(playlistId)
                            currentIndex = startIndex
                            if (trackList.isNotEmpty()) {
                                playTrack(currentIndex)
                                updateSession()
                            }
                        }
                    } else {
                        mediaSession.controller.transportControls.stop()
                    }
                }
                else -> super.onStartCommand(intent, flags, startId)
            }
        }
        return START_STICKY
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        return BrowserRoot("root", null)
    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == "root") {
            val items = trackList.map { track ->
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(track.trackId.toString())
                        .setTitle(track.trackName)
                        .setSubtitle(track.artist)
                        .setIconUri(Uri.parse(track.cover))
                        .build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            }
            result.sendResult(items.toMutableList())
        } else {
            result.sendResult(mutableListOf())
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "AudioPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    val index = trackList.indexOfFirst { it.trackId.toString() == mediaId }
                    if (index >= 0) {
                        currentIndex = index
                        playTrack(index)
                        updateSession()
                    }
                }
                override fun onPlay() = playPause()
                override fun onPause() = playPause()
                override fun onSkipToNext() = skipToNext()
                override fun onSkipToPrevious() = skipToPrev()
                override fun onStop() {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    private fun playPause() {
        if (mediaPlayer == null) {
            serviceScope.launch {
                trackList = playlistDao.getAllTracks().first()
                if (trackList.isEmpty()) return@launch
                playTrack(currentIndex)
                updateSession()
            }
        } else {
            mediaPlayer!!.apply {
                if (isPlaying) pause() else start()
            }
            updateSession()
        }
    }

    private fun playTrack(index: Int) {
        val track = trackList[index]
        playbackRepo.updateCurrentTrack(track)
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@AudioPlayerService, Uri.parse(track.fileURI))
            prepare()
            start()
            setOnCompletionListener { skipToNext() }
        }
    }

    private fun skipToNext() {
        if (trackList.isEmpty()) return
        currentIndex = (currentIndex + 1) % trackList.size
        playTrack(currentIndex)
        updateSession()
    }

    private fun skipToPrev() {
        if (trackList.isEmpty()) return
        currentIndex = if (currentIndex == 0) trackList.lastIndex else currentIndex - 1
        playTrack(currentIndex)
        updateSession()
    }

    private fun updateSession() {
        serviceScope.launch {
            val isPlaying = mediaPlayer?.isPlaying == true
            val currentPos = mediaPlayer?.currentPosition?.toLong() ?: 0L
            val duration = mediaPlayer?.duration?.toLong() ?: 0L
            val state =
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            playbackRepo.updateIsPlaying(isPlaying)

            playbackStateBuilder
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP
                )
                .setState(state, currentPos, 1.0f)

            mediaSession.setPlaybackState(playbackStateBuilder.build())

            var coverBitmap: Bitmap? = null
            trackList.getOrNull(currentIndex)?.let { track ->
                mediaSession.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.trackName)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .build()
                )
                coverBitmap = loadCoverBitmap(track.cover)
            }

            startForeground(NOTIF_ID, buildNotification(isPlaying, coverBitmap))
        }
    }

    private suspend fun loadCoverBitmap(url: String?): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            if (result is SuccessResult) result.image.toBitmap()
            else null
        } catch (e: Exception) {
            null
        }
    }

    private fun buildNotification(isPlaying: Boolean, cover: Bitmap?): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        fun pi(action: String) = PendingIntent.getService(
            this, 0,
            Intent(this, AudioPlayerService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val trackTitle = trackList.getOrNull(currentIndex)?.trackName ?: "Audio Player"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(openAppIntent)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(trackTitle)
            .addAction(android.R.drawable.ic_media_previous, "Previous", pi(ACTION_PREV))
            .addAction(playPauseIcon, "Play/Pause", pi(ACTION_PLAY_PAUSE))
            .addAction(android.R.drawable.ic_media_next, "Next", pi(ACTION_NEXT))
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(pi(ACTION_STOP))
            )
            .setOngoing(isPlaying)
            .apply { setLargeIcon(cover) }
            .build()
    }

    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            CHANNEL_ID,
            "Audio Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Controls for audio playback"
        }
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(chan)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaSession.release()
        serviceScope.cancel()
        super.onDestroy()
    }
}
