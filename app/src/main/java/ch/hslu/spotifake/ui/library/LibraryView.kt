package ch.hslu.spotifake.ui.library

import android.R.attr.maxLines
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.colorResource
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

    if (selectedPlaylistId == null) {

        val playlists by viewModel.allPlaylists.observeAsState(emptyList())
        val likedSongsPlaylist by viewModel.likedSongsPlaylist.collectAsState(
            initial = PlaylistWithTracks(
                playlist = Playlist(0, "Liked Songs"),
                tracks = emptyList()
            )
        )

        Column {
            if (showCreateDialog) {
                CreatePlaylistDialog(
                    onCreate = { name ->
                        viewModel.createPlaylist(name)
                        showCreateDialog = false
                    },
                    onDismiss = { showCreateDialog = false }
                )
            }

            LazyColumn {
                item {
                    likedSongsPlaylist?.playlist?.let {
                        Text(
                            text = it.playlistName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlaylistId = 0 }
                                .padding(16.dp)
                        )
                    }
                }
                items(playlists.size) {
                    index ->
                    Text(
                        text = playlists[index].playlist.playlistName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlaylistId = playlists[index].playlist.playlistId }
                            .padding(16.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
                {
                    Text("Create Playlist")
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
                Text("Playlist: ${playlist.playlist.playlistName}", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(playlist.tracks.size) {
                        index ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = playlist.tracks[index].trackName + ", Id = " + playlist.tracks[index].trackId,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            IconButton(onClick = {
                                trackToAdd = playlist.tracks[index]
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add to playlist")
                            }

                            IconButton(onClick = {
                                viewModel.deleteTrack(playlist.tracks[index])
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete track", tint = Color.Red)
                            }

                            IconButton(onClick = {
                                viewModel.removeTrackFromPlaylist(
                                    trackId = playlist.tracks[index].trackId,
                                    playlistId = playlist.playlist.playlistId
                                )
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove from playlist", tint = Color.Blue)
                            }
                        }
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