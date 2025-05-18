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
fun LibraryView(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylistId by remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var trackToAdd by remember { mutableStateOf<Track?>(null) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    if (selectedPlaylistId == null) {

        val playlists by viewModel.allPlaylists.observeAsState(emptyList())
        val likedSongsPlaylist by viewModel.likedSongsPlaylist.collectAsState(
            initial = PlaylistWithTracks(
                playlist = Playlist(0, "Liked Songs"),
                tracks = emptyList()
            )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            if (showCreateDialog) {
                CreatePlaylistDialog(
                    onCreate = { name ->
                        viewModel.createPlaylist(name)
                        showCreateDialog = false
                    },
                    onDismiss = { showCreateDialog = false }
                )
            }

            Text("My Library", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                item {
                    likedSongsPlaylist?.playlist?.let {
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
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Track",
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Text(
                                    text = it.playlistName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPlaylistId = 0 }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
                items(playlists.size) {
                    index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = playlists[index].playlist.playlistName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPlaylistId = playlists[index].playlist.playlistId
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
                {
                    Icon(Icons.Default.Add, contentDescription = "Add playlist")
                }
            }
        }
    } else {
        val playlistFlow = remember(selectedPlaylistId) {
            if(selectedPlaylistId == 0) {
                viewModel.getAllTracks()
            } else {
                viewModel.getPlaylistWithTracks(selectedPlaylistId!!)
            }
        }
        val playlistWithTracks by playlistFlow.collectAsState(initial = null)

        playlistWithTracks?.let { playlist ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${playlist.playlist.playlistName}", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    if (playlist.tracks.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = Color.Gray)
                            Text("Pretty lonely in here...", color = Color.Gray)
                        }
                    }

                    LazyColumn {
                        items(playlist.tracks.size) { index ->
                            TrackItem(
                                track = playlist.tracks[index],
                                playlistId = playlist.playlist.playlistId,
                                onAddToPlaylist = {
                                    trackToAdd = playlist.tracks[index]
                                    showDialog = true
                                },
                                onRemoveFromPlaylist = {
                                    viewModel.removeTrackFromPlaylist(
                                        playlist.tracks[index].trackId,
                                        playlist.playlist.playlistId
                                    )
                                },
                                onDelete = {
                                    trackToDelete = playlist.tracks[index]
                                }
                            )
                        }
                    }

                    if (showDialog && trackToAdd != null) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Add to Playlist") },
                            text = {
                                val playlists by viewModel.allPlaylists.observeAsState(emptyList())

                                Column {
                                    playlists.forEach { playlist ->
                                        Text(
                                            text = playlist.playlist.playlistName,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.addTrackToPlaylist(
                                                        trackId = trackToAdd!!.trackId,
                                                        playlistId = playlist.playlist.playlistId
                                                    )
                                                    showDialog = false
                                                    trackToAdd = null
                                                }
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                    trackToAdd = null
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

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

                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { selectedPlaylistId = null },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                        {
                            Text("Back to Playlists")
                        }
                    }
                }
            }
        }
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