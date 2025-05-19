package ch.hslu.spotifake.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.PlaylistDao
import ch.hslu.spotifake.db.PlaylistTrackCrossReference
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    playlistDao: PlaylistDao
) : ViewModel() {
    private val dao = playlistDao

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

    fun addTrackToPlaylist(playlistId: Int, trackId: Int) = viewModelScope.launch {
        dao.addTrackToPlaylist(PlaylistTrackCrossReference(playlistId, trackId))
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            dao.deleteTrack(track)
        }
    }

    fun getPlaylistWithTracks(id: Int): Flow<PlaylistWithTracks> {
        return dao.getPlaylistWithTracks(id)
    }

    fun getAllTracks(): Flow<PlaylistWithTracks> {
        return dao.getAllTracks().map { trackList ->
            PlaylistWithTracks(
                playlist = Playlist(0, "All Songs"),
                tracks = trackList
            )
        }
    }

    fun removeTrackFromPlaylist(trackId: Int, playlistId: Int) {
        viewModelScope.launch {
            dao.removeTrackFromPlaylist(playlistId, trackId)
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