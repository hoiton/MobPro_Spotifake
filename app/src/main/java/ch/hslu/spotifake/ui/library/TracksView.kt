package ch.hslu.spotifake.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track

@Composable
fun TracksView(
    playlistWithTracks: PlaylistWithTracks,
    onRemoveFromPlaylist: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    onBack: () -> Unit,
    dialogState: Pair<Boolean, Track?>,
    onDialogDismiss: () -> Unit,
    onTrackSelectedToAdd: (Track?) -> Unit,
    playlists: List<PlaylistWithTracks>,
    onAddTrackToPlaylist: (Int, Int) -> Unit,
    onPlayAll: (List<Track>) -> Unit
) {
    val (showDialog, trackToAdd) = dialogState

    Column(modifier = Modifier.padding(16.dp)) {
        Text("${playlistWithTracks.playlist.playlistName}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (playlistWithTracks.tracks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MusicOff, contentDescription = null, tint = Color.Gray)
                    Text("Pretty lonely in here...", color = Color.Gray)
                }
            } else {
                PlaybackControls(
                    playlistWithTracks = playlistWithTracks,
                    onPlayAll = { tracks ->
                        // TODO
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 76.dp)
                ) {
                    items(playlistWithTracks.tracks.size) { index ->
                        val track = playlistWithTracks.tracks[index]
                        TrackItem(
                            track = track,
                            playlistId = playlistWithTracks.playlist.playlistId,
                            onAddToPlaylist = { onTrackSelectedToAdd(track) },
                            onRemoveFromPlaylist = { onRemoveFromPlaylist(track) },
                            onDelete = { onDeleteTrack(track) }
                        )
                    }
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
                                            onAddTrackToPlaylist(trackToAdd.trackId, playlist.playlist.playlistId)
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

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("Back to Playlists")
            }
        }
    }
}
