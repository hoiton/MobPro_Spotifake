package ch.hslu.spotifake.ui.library

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.PlaylistWithTracks

@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel,
    onPlaylistSelected: (Int) -> Unit
) {
    val playlists by viewModel.allPlaylists.observeAsState(emptyList())
    val likedSongsPlaylist by viewModel.likedSongsPlaylist.collectAsState(
        initial = PlaylistWithTracks(playlist = Playlist(0, "All Songs"), tracks = emptyList())
    )
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    PlaylistView(
        playlists = playlists,
        likedSongsPlaylist = likedSongsPlaylist,
        onCreatePlaylist = { viewModel.createPlaylist(it) },
        onPlaylistClick = onPlaylistSelected,
        showCreateDialog = showCreateDialog,
        onDismissCreateDialog = { showCreateDialog = false },
        onShowCreateDialog = { showCreateDialog = true },
        onDeletePlaylist = { playlistToDelete = it }
    )

    if (playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete \"${playlistToDelete!!.playlistName}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlaylist(playlistToDelete!!)
                    playlistToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}