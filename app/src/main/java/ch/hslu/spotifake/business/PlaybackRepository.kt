package ch.hslu.spotifake.business

import ch.hslu.spotifake.db.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackRepository @Inject constructor() {
    private val _currentTrack = MutableStateFlow<Track?>(null)
    private val _isPlaying = MutableStateFlow(false)

    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun updateCurrentTrack(track: Track?) {
        _currentTrack.value = track
    }
    fun updateIsPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}