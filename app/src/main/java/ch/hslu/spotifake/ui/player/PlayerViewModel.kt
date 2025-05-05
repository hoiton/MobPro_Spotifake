package ch.hslu.spotifake.ui.player

import android.app.Application
import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import ch.hslu.spotifake.business.player.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

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