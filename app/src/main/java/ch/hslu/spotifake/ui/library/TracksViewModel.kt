package ch.hslu.spotifake.ui.library

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.business.AudioPlayerService
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.LibraryDao
import ch.hslu.spotifake.db.PlaylistTrackCrossReference
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    libraryDao: LibraryDao
) : ViewModel() {
    private val dao = libraryDao

    private val _likedSongsPlaylist = MutableStateFlow<PlaylistWithTracks?>(null)
    val allPlaylists: LiveData<List<PlaylistWithTracks>> = dao.getAllPlaylistsWithTracks().asLiveData()

    init {
        viewModelScope.launch {
            val playlist = loadLikedSongsPlaylist()
            _likedSongsPlaylist.value = playlist
        }
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

    fun playTrack(tracks: List<Track>, index: Int) {
        Intent(context, AudioPlayerService::class.java).also {
            it.action = AudioPlayerService.ACTION_PLAY_TRACKS
            it.putExtra(
                AudioPlayerService.EXTRA_TRACK_IDS,
                tracks.map { track -> track.trackId }.toIntArray()
            )
            it.putExtra(AudioPlayerService.EXTRA_START_INDEX, index)
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