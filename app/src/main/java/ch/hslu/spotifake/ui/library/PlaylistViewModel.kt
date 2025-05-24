package ch.hslu.spotifake.ui.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.LibraryDao
import ch.hslu.spotifake.db.PlaylistWithTracks
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    libraryDao: LibraryDao
) : ViewModel() {
    private val dao = libraryDao

    private val _likedSongsPlaylist = MutableStateFlow<PlaylistWithTracks?>(null)
    val likedSongsPlaylist: StateFlow<PlaylistWithTracks?> = _likedSongsPlaylist
    val allPlaylists: LiveData<List<PlaylistWithTracks>> = dao.getAllPlaylistsWithTracks().asLiveData()

    init {
        viewModelScope.launch {
            val playlist = loadLikedSongsPlaylist()
            _likedSongsPlaylist.value = playlist
        }
    }

    fun createPlaylist(name: String) = viewModelScope.launch {
        dao.insertPlaylist(Playlist(playlistId = 0, playlistName = name))
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            dao.deletePlaylist(playlist)
        }
    }

    private suspend fun loadLikedSongsPlaylist(): PlaylistWithTracks {
        val tracksList = dao.getAllTracks().first()
        return PlaylistWithTracks(
            playlist = Playlist(0, "All Songs"),
            tracks = tracksList
        )
    }
}