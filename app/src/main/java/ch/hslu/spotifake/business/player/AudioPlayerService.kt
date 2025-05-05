package ch.hslu.spotifake.business.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class AudioPlayerService : Service() {

    companion object {
        private const val CHANNEL_ID    = "audio_playback_channel"
        private const val NOTIF_ID      = 1

        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT       = "ACTION_NEXT"
        const val ACTION_PREV       = "ACTION_PREV"
        const val ACTION_STOP       = "ACTION_STOP"
    }

    private val binder = LocalBinder()
    private inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    private val audioFiles: List<File> by lazy {
        filesDir.listFiles { f ->
            when (f.extension.lowercase()) {
                "mp3","wav","ogg","m4a" -> true
                else -> false
            }
        }?.toList().orEmpty()
    }
    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { handleAction(it) }
        return START_STICKY
    }

    private fun handleAction(action: String) {
        when (action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT       -> skipToNext()
            ACTION_PREV       -> skipToPrev()
            ACTION_STOP       -> {
                stopSelf()
                return
            }
        }
        // always update the foreground notification to reflect current state
        startForeground(NOTIF_ID, buildNotification(mediaPlayer?.isPlaying == true))
    }

    private fun togglePlayPause() {
        if (mediaPlayer == null) {
            if (audioFiles.isEmpty()) return
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFiles[currentIndex].absolutePath)
                prepare()
                start()
                setOnCompletionListener { skipToNext() }
            }
        } else {
            mediaPlayer!!.let {
                if (it.isPlaying) it.pause() else it.start()
            }
        }
    }

    private fun skipToNext() {
        mediaPlayer?.release()
        // ToDo: what if no file is playing?
        currentIndex = (currentIndex + 1) % audioFiles.size
        mediaPlayer = null
        togglePlayPause()
    }

    private fun skipToPrev() {
        mediaPlayer?.release()
        currentIndex = if (currentIndex == 0) audioFiles.lastIndex else currentIndex - 1
        mediaPlayer = null
        togglePlayPause()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        fun actionIntent(act: String) =
            PendingIntent.getService(
                this, 0,
                Intent(this, AudioPlayerService::class.java).setAction(act),
                PendingIntent.FLAG_IMMUTABLE
            )

        val playPauseIcon = if (isPlaying)
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play

        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Audio Player")
            .setContentText(audioFiles.getOrNull(currentIndex)?.name ?: "")
            .addAction(
                android.R.drawable.ic_media_previous,
                "Prev",
                actionIntent(ACTION_PREV)
            )
            .addAction(
                playPauseIcon,
                playPauseTitle,
                actionIntent(ACTION_PLAY_PAUSE)
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Next",
                actionIntent(ACTION_NEXT)
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
            // Do not show badge on app icon, because it is not an ordinary notification
            setShowBadge(false)
        }
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(chan)
    }
}
