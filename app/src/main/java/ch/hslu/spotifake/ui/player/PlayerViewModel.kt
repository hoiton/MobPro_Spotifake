package ch.hslu.spotifake.ui.player

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // 1. Find all audio files in filesDir
    private val audioFiles: List<File> = context.filesDir
        .listFiles { f ->
            val ext = f.extension.lowercase()
            ext in listOf("mp3", "wav", "m4a", "ogg")
        }
        ?.toList()
        .orEmpty()

    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null

    // 2. Expose playing state for the UI
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // 3. Play or pause the current track (first on first tap)
    fun playOrPause() {
        if (mediaPlayer == null) {
            // first‐time play
            if (audioFiles.isNotEmpty()) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFiles[currentIndex].absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        _isPlaying.value = false
                    }
                }
                _isPlaying.value = true
            }
        } else {
            mediaPlayer!!.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                } else {
                    mp.start()
                    _isPlaying.value = true
                }
            }
        }
    }

    // 4. Advance to next track
    fun next() {
        if (audioFiles.isEmpty()) return
        mediaPlayer?.release()
        currentIndex = (currentIndex + 1) % audioFiles.size
        mediaPlayer = null
        playOrPause()
    }

    // 5. Go to previous track
    fun prev() {
        if (audioFiles.isEmpty()) return
        mediaPlayer?.release()
        currentIndex = if (currentIndex == 0) audioFiles.lastIndex else currentIndex - 1
        mediaPlayer = null
        playOrPause()
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}