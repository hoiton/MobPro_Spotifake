package ch.hslu.spotifake.ui.player

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import ch.hslu.spotifake.business.AudioPlayerService
import ch.hslu.spotifake.business.PlaybackRepository
import ch.hslu.spotifake.db.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    playbackRepo: PlaybackRepository,
) : ViewModel() {

    val currentTrack: StateFlow<Track?> = playbackRepo.currentTrack
    val isPlaying: StateFlow<Boolean> = playbackRepo.isPlaying

    fun playOrPause() {
        val action = AudioPlayerService.ACTION_PLAY_PAUSE
        context.startService(Intent(context, AudioPlayerService::class.java).setAction(action))
    }
    fun next() = context.startService(
        Intent(context, AudioPlayerService::class.java).setAction(AudioPlayerService.ACTION_NEXT)
    )
    fun prev() = context.startService(
        Intent(context, AudioPlayerService::class.java).setAction(AudioPlayerService.ACTION_PREV)
    )
}