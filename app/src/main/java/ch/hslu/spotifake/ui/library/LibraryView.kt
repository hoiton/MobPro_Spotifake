package ch.hslu.spotifake.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.hslu.spotifake.db.Playlist
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track

@Composable
fun LibraryView(viewModel: LibraryViewModel = hiltViewModel()) {
    var selectedPlaylistId by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var trackToAdd by remember { mutableStateOf<Track?>(null) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    val playlists by viewModel.allPlaylists.observeAsState(emptyList())
    val likedSongsPlaylist by viewModel.likedSongsPlaylist.collectAsState(
        initial = PlaylistWithTracks(playlist = Playlist(0, "All Songs"), tracks = emptyList())
    )

    if (selectedPlaylistId == null) {
        PlaylistView(
            playlists = playlists,
            likedSongsPlaylist = likedSongsPlaylist,
            onCreatePlaylist = { viewModel.createPlaylist(it) },
            onPlaylistClick = { selectedPlaylistId = it },
            showCreateDialog = showCreateDialog,
            onDismissCreateDialog = { showCreateDialog = false },
            onShowCreateDialog = { showCreateDialog = true }
        )
    } else {
        val playlistFlow = remember(selectedPlaylistId) {
            if (selectedPlaylistId == 0) viewModel.getAllTracks()
            else viewModel.getPlaylistWithTracks(selectedPlaylistId!!)
        }
        val playlistWithTracks by playlistFlow.collectAsState(initial = null)

        playlistWithTracks?.let {
            TracksView(
                playlistWithTracks = it,
                onAddToPlaylist = { trackToAdd = it; showDialog = true },
                onRemoveFromPlaylist = { track ->
                    viewModel.removeTrackFromPlaylist(track.trackId, it.playlist.playlistId)
                },
                onDeleteTrack = { trackToDelete = it },
                onBack = { selectedPlaylistId = null },
                dialogState = showDialog to trackToAdd,
                onDialogDismiss = { showDialog = false; trackToAdd = null },
                onTrackSelectedToAdd = { trackToAdd = it; showDialog = true },
                playlists = playlists,
                onAddTrackToPlaylist = { trackId, playlistId ->
                    viewModel.addTrackToPlaylist(trackId, playlistId)
                }
            )
        }
    }

    // Delete confirmation dialog outside the conditionals
    if (trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = { Text("Delete Track") },
            text = { Text("Are you sure you want to permanently delete \"${trackToDelete!!.trackName}\"?") },
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


@Composable
fun CreatePlaylistDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Playlist name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onCreate(text.trim())
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TrackItem(
    track: Track,
    playlistId: Int?,
    onAddToPlaylist: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = track.trackName,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Track menu")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to other playlist") },
                    onClick = {
                        expanded = false
                        onAddToPlaylist()
                    }
                )

                if (playlistId != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from this playlist") },
                        onClick = {
                            expanded = false
                            onRemoveFromPlaylist()
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        expanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}