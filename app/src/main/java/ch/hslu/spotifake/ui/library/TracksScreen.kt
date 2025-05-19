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
import androidx.navigation.NavHostController
import ch.hslu.spotifake.SpotifakeNavHost
import ch.hslu.spotifake.db.Track
import ch.hslu.spotifake.ui.navigation.SpotifakeScreens

@Composable
fun TracksScreen(
    navHostController: NavHostController,
    viewModel: LibraryViewModel,
    playlistId: Int,
    isPrimaryLibrary: Boolean,
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

    playlistWithTracks?.let { playlist ->
        TracksView(
            playlistWithTracks = playlist,
            onRemoveFromPlaylist = { track ->
                viewModel.removeTrackFromPlaylist(track.trackId, playlistId)
            },
            onDeleteTrack = { trackToDelete = it },
            dialogState = showDialog to trackToAdd,
            onDialogDismiss = { showDialog = false; trackToAdd = null },
            onTrackSelectedToAdd = { trackToAdd = it; showDialog = true },
            playlists = playlists,
            onAddTrackToPlaylist = { trackId, targetPlaylistId ->
                viewModel.addTrackToPlaylist(trackId, targetPlaylistId)
            },
            onPlayAll = { viewModel.playAllTracks(it) },
            onPlayTrack = { tracks, index -> viewModel.playTrack(tracks, index) },
            onAddTrack = { navHostController.navigate(SpotifakeScreens.Upload.route) },
            isPrimaryLibrary = isPrimaryLibrary,
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