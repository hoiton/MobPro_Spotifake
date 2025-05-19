package ch.hslu.spotifake.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.PlaylistWithTracks

@Composable
fun PlaylistScreen(
    viewModel: LibraryViewModel,
    onPlaylistSelected: (Int) -> Unit
) {
    val playlists by viewModel.allPlaylists.observeAsState(emptyList())
    val likedSongsPlaylist by viewModel.likedSongsPlaylist.collectAsState(
        initial = PlaylistWithTracks(playlist = Playlist(0, "All Songs"), tracks = emptyList())
    )
    var showCreateDialog by remember { mutableStateOf(false) }

    PlaylistView(
        playlists = playlists,
        likedSongsPlaylist = likedSongsPlaylist,
        onCreatePlaylist = { viewModel.createPlaylist(it) },
        onPlaylistClick = onPlaylistSelected,
        showCreateDialog = showCreateDialog,
        onDismissCreateDialog = { showCreateDialog = false },
        onShowCreateDialog = { showCreateDialog = true }
    )
}