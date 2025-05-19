package ch.hslu.spotifake.ui.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.business.AudioPlayerService
import ch.hslu.spotifake.db.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var service: AudioPlayerService? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as AudioPlayerService.LocalBinder).getService()
            service?.currentTrackFlow
                ?.onEach { _currentTrack.value = it }
                ?.launchIn(viewModelScope)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    init {
        Intent(context, AudioPlayerService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            context.startService(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        service?.let {
            context.unbindService(connection)
        }
    }

    fun playOrPause() {
        service?.let {
            val action = AudioPlayerService.ACTION_PLAY_PAUSE
            context.startService(Intent(context, AudioPlayerService::class.java).setAction(action))
        }
    }
    fun next() = context.startService(
        Intent(context, AudioPlayerService::class.java).setAction(AudioPlayerService.ACTION_NEXT)
    )
    fun prev() = context.startService(
        Intent(context, AudioPlayerService::class.java).setAction(AudioPlayerService.ACTION_PREV)
    )
}