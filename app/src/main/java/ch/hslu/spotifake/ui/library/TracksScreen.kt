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
import ch.hslu.spotifake.db.Track

@Composable
fun TracksScreen(
    viewModel: LibraryViewModel,
    playlistId: Int,
    onBack: () -> Unit
) {
    val playlistFlow = remember(playlistId) {
        if (playlistId == 0) viewModel.getAllTracks()
        else viewModel.getPlaylistWithTracks(playlistId)
    }
    val playlistWithTracks by playlistFlow.collectAsState(initial = null)
    val playlists by viewModel.allPlaylists.observeAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var trackToAdd by remember { mutableStateOf<Track?>(null) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    playlistWithTracks?.let {
        TracksView(
            playlistWithTracks = it,
            onRemoveFromPlaylist = { track ->
                viewModel.removeTrackFromPlaylist(track.trackId, playlistId)
            },
            onDeleteTrack = { trackToDelete = it },
            onBack = onBack,
            dialogState = showDialog to trackToAdd,
            onDialogDismiss = { showDialog = false; trackToAdd = null },
            onTrackSelectedToAdd = { trackToAdd = it; showDialog = true },
            playlists = playlists,
            onAddTrackToPlaylist = { trackId, targetPlaylistId ->
                viewModel.addTrackToPlaylist(trackId, targetPlaylistId)
            }
        )
    }

    if (trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = { Text("Delete Track") },
            text = { Text("Are you sure you want to delete \"${trackToDelete!!.trackName}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTrack(trackToDelete!!)
                    trackToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}