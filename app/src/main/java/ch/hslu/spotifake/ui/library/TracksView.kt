package ch.hslu.spotifake.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track

@Composable
fun TracksView(
    playlistWithTracks: PlaylistWithTracks,
    onRemoveFromPlaylist: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    dialogState: Pair<Boolean, Track?>,
    onDialogDismiss: () -> Unit,
    onTrackSelectedToAdd: (Track?) -> Unit,
    playlists: List<PlaylistWithTracks>,
    onAddTrackToPlaylist: (Int, Int) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onPlayTrack: (List<Track>, Int) -> Unit,
    onAddTrack: () -> Unit,
    isPrimaryLibrary: Boolean,
    navController: NavHostController
) {
    val (showDialog, trackToAdd) = dialogState

    var filterText by remember { mutableStateOf("") }

    val filteredTracks = playlistWithTracks.tracks.filter {
        it.trackName.contains(filterText, ignoreCase = true)
                || it.artist.contains(filterText, ignoreCase = true)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isPrimaryLibrary) {
                IconButton(
                    onClick = {
                        navController.navigateUp()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                    )
                }

            }
            Text(
                text = playlistWithTracks.playlist.playlistName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
                    .padding(vertical = 8.dp)
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (playlistWithTracks.tracks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MusicOff, contentDescription = null, tint = Color.Gray)
                    Text("Pretty lonely in here...", color = Color.Gray)
                    Text("Why not add some Tracks?", color = Color.Gray)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    PlaybackControls(
                        playlistWithTracks = playlistWithTracks,
                        onPlayAll = onPlayAll,
                        modifier = Modifier.fillMaxWidth(),
                        onFilterValueChanged = { filterText = it }
                    )

                    if (filteredTracks.isEmpty())
                    {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MusicOff, contentDescription = null, tint = Color.Gray)
                            Text("No tracks found...", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredTracks) { track ->
                                val originalIndex = playlistWithTracks.tracks.indexOf(track)

                                TrackItem(
                                    playlistWithTracks = playlistWithTracks,
                                    index = originalIndex,
                                    track = track,
                                    playlistId = playlistWithTracks.playlist.playlistId,
                                    onAddToPlaylist = { onTrackSelectedToAdd(track) },
                                    onRemoveFromPlaylist = { onRemoveFromPlaylist(track) },
                                    onDelete = { onDeleteTrack(track) },
                                    onPlayClick = { _, _ ->
                                        onPlayTrack(
                                            playlistWithTracks.tracks,
                                            originalIndex
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isPrimaryLibrary) {
                FloatingActionButton(
                    onClick = onAddTrack,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add new Track")
                }
            }

            if (showDialog && trackToAdd != null) {
                AlertDialog(
                    onDismissRequest = onDialogDismiss,
                    title = { Text("Add to Playlist") },
                    text = {
                        Column {
                            playlists.forEach { playlist ->
                                Text(
                                    text = playlist.playlist.playlistName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddTrackToPlaylist(
                                                trackToAdd.trackId,
                                                playlist.playlist.playlistId
                                            )
                                            onDialogDismiss()
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = onDialogDismiss) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
