package ch.hslu.spotifake.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.spotifake.db.PlaylistWithTracks
import ch.hslu.spotifake.db.Track

@Composable
fun PlaybackControls(
    playlistWithTracks: PlaylistWithTracks,
    onPlayAll: (List<Track>) -> Unit,
    modifier: Modifier = Modifier
) {
    var isShuffleOn by remember { mutableStateOf(false) }
    var isRepeatOn by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FloatingActionButton(
            onClick = { onPlayAll(playlistWithTracks.tracks) },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play All")
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { isShuffleOn = !isShuffleOn }) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffleOn) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { isRepeatOn = !isRepeatOn }) {
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeatOn) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}