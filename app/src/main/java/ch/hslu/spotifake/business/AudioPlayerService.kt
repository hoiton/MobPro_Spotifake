package ch.hslu.spotifake.business

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import ch.hslu.spotifake.MainActivity
import ch.hslu.spotifake.db.LibraryDao
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

    @Inject lateinit var libraryDao: LibraryDao
    @Inject lateinit var playbackRepo: PlaybackRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var trackList: List<Track> = emptyList()
    private var originalTrackList: List<Track> = emptyList()
    private var currentIndex = 0

    private lateinit var mediaSession: MediaSessionCompat
    private val playbackStateBuilder = PlaybackStateCompat.Builder()
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var audioManager: AudioManager
    private lateinit var afRequest: AudioFocusRequest

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 1a) Build the focus-request with your media attributes
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        afRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        // resumed by Auto reconnect, or regained after duck
                        mediaPlayer?.apply {
                            if (!isPlaying) start()
                        }
                        updateSession()
                    }
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        // permanent loss: stop completely
                        mediaSession.controller.transportControls.pause()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        // brief duck: pause but remember to resume
                        if (mediaPlayer?.isPlaying == true) {
                            mediaSession.controller.transportControls.pause()
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        // duck volume
                        mediaPlayer?.setVolume(0.2f, 0.2f)
                    }
                }
            }
            .build()


        serviceScope.launch {
            libraryDao.getAllTracks().collect { tracks ->
                trackList = tracks
                originalTrackList = tracks
                if (currentIndex >= trackList.size) {
                    currentIndex = 0
                }

                notifyChildrenChanged("root")
            }
        }

        serviceScope.launch {
            playbackRepo.shuffle.collect {
                updateTrackList()
                notifyChildrenChanged("root")
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
                            trackList = libraryDao.loadAllTracksByIds(playlistId)
                            originalTrackList = trackList
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
        Log.i("AudioPlayerService", "onGetRoot called with package: $clientPackageName, uid: $clientUid")
        return BrowserRoot("root", null)
    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.i("AudioPlayerService", "onLoadChildren called with parentId: $parentId")
        if (parentId == "root") {
            val items = trackList.map { track ->
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(track.trackId.toString())
                        .setTitle(track.trackName)
                        .setSubtitle(track.artist)
                        .setIconUri(Uri.parse(track.cover?: Track.DEFAULT_COVER_URL))
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

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayerService").apply {
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
                    audioManager.abandonAudioFocusRequest(afRequest)
                    mediaPlayer?.release()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            setSessionToken(sessionToken)
            isActive = true
        }
    }

    private fun updateTrackList() {
        trackList = if (playbackRepo.shuffle.value) {
            originalTrackList.shuffled().toMutableList()
        } else {
            originalTrackList.toMutableList()
        }

        val currentTrack = playbackRepo.currentTrack.value
        currentIndex = trackList.indexOfFirst { it.trackId == currentTrack?.trackId }
    }

    private fun playPause() {
        if (mediaPlayer == null) {
            serviceScope.launch {
                trackList = libraryDao.getAllTracks().first()
                originalTrackList = trackList
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
        val test = if (index < 0) 0 else index
        if (index < 0) {
            Log.e("AudioPlayerService", "Invalid track index: $index")
        }
        val track = trackList[test]
        playbackRepo.updateCurrentTrack(track)

        if (audioManager.requestAudioFocus(afRequest)
            != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioManager.abandonAudioFocusRequest(afRequest)
            return
        }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(this@AudioPlayerService, Uri.parse(track.fileURI))
            setOnPreparedListener { mp ->
                mp.start()
                updateSession()
            }
            prepareAsync()
            setOnCompletionListener { skipToNext() }
        }
    }

    private fun skipToNext() {
        if (trackList.isEmpty()) return
        if (!playbackRepo.repeat.value) {
            currentIndex = (currentIndex + 1) % trackList.size
        }
        playTrack(currentIndex)
    }

    private fun skipToPrev() {
        if (trackList.isEmpty()) return
        if (!playbackRepo.repeat.value) {
            currentIndex = if (currentIndex == 0) trackList.lastIndex else currentIndex - 1
        }
        playTrack(currentIndex)
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
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.cover ?: Track.DEFAULT_COVER_URL)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, track.cover ?: Track.DEFAULT_COVER_URL)
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
