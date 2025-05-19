package ch.hslu.spotifake.ui.library

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.business.AudioPlayerService
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.PlaylistDao
import ch.hslu.spotifake.db.PlaylistTrackCrossReference
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    fun addTrackToPlaylist(trackId: Int, playlistId: Int) = viewModelScope.launch {
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

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            dao.deletePlaylist(playlist)
        }
    }

    fun playAllTracks(tracks: List<Track>) {
        Intent(context, AudioPlayerService::class.java).also {
            it.action = AudioPlayerService.ACTION_PLAY_TRACKS
            it.putExtra(
                AudioPlayerService.EXTRA_TRACK_IDS,
                tracks.map { track -> track.trackId }.toIntArray()
            )
            context.startService(it)
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