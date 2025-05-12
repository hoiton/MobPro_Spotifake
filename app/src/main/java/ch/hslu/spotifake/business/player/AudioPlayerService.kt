package ch.hslu.spotifake.business.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import java.io.File

class AudioPlayerService : Service() {

    companion object {
        private const val CHANNEL_ID = "audio_playback_channel"
        private const val NOTIF_ID = 1

        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_STOP = "ACTION_STOP"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private val playbackStateBuilder = PlaybackStateCompat.Builder()
    private var mediaPlayer: MediaPlayer? = null
    private val audioFiles: List<File> by lazy {
        filesDir.listFiles { f ->
            when (f.extension.lowercase()) {
                "mp3", "wav", "ogg", "m4a" -> true
                else -> false
            }
        }?.toList().orEmpty()
    }
    private var currentIndex = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder? = null

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
            }
        }
        return START_STICKY
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "AudioPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = playPause()
                override fun onPause() = playPause()
                override fun onSkipToNext() = skipToNext()
                override fun onSkipToPrevious() = skipToPrev()
                override fun onStop() = stopSelf()
            })
            isActive = true
        }
    }

    private fun playPause() {
        if (mediaPlayer == null) {
            if (audioFiles.isEmpty()) return
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFiles[currentIndex].absolutePath)
                prepare()
                start()
                setOnCompletionListener { skipToNext() }
            }
        } else {
            mediaPlayer!!.apply {
                if (isPlaying) pause() else start()
            }
        }
        updateSession()
    }

    private fun skipToNext() {
        mediaPlayer?.release()
        if (audioFiles.isEmpty()) return
        currentIndex = (currentIndex + 1) % audioFiles.size
        mediaPlayer = null
        playPause()
    }

    private fun skipToPrev() {
        mediaPlayer?.release()
        if (audioFiles.isEmpty()) return
        currentIndex = if (currentIndex == 0) audioFiles.lastIndex else currentIndex - 1
        mediaPlayer = null
        playPause()
    }

    private fun updateSession() {
        val isPlaying = mediaPlayer?.isPlaying == true
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED

        playbackStateBuilder
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, mediaPlayer?.currentPosition?.toLong() ?: 0L, 1.0f)
        mediaSession.setPlaybackState(playbackStateBuilder.build())

        val title = audioFiles.getOrNull(currentIndex)?.name ?: ""
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build()
        )

        startForeground(NOTIF_ID, buildNotification(isPlaying))
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        fun pi(action: String) = PendingIntent.getService(
            this, 0,
            Intent(this, AudioPlayerService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Audio Player")
            .setContentText(audioFiles.getOrNull(currentIndex)?.name ?: "")
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
            .build()
    }

    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            CHANNEL_ID,
            "Audio Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Controls for audio playback"
//            setShowBadge(false)
        }
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(chan)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaSession.release()
        super.onDestroy()
    }
}

