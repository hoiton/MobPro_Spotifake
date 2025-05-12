package ch.hslu.spotifake.ui.player

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import ch.hslu.spotifake.business.AudioPlayerService

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application.applicationContext

    fun playOrPause() =
        ctx.startService(
            Intent(ctx, AudioPlayerService::class.java)
                .setAction(AudioPlayerService.ACTION_PLAY_PAUSE)
        )

    fun next() =
        ctx.startService(
            Intent(ctx, AudioPlayerService::class.java)
                .setAction(AudioPlayerService.ACTION_NEXT)
        )

    fun prev() =
        ctx.startService(
            Intent(ctx, AudioPlayerService::class.java)
                .setAction(AudioPlayerService.ACTION_PREV)
        )
}