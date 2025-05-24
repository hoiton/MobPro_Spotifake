package ch.hslu.spotifake.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.hslu.spotifake.db.Track
import coil3.compose.AsyncImage

@Preview(showBackground = true)
@Composable
fun TestPlayerScreen() {
    PlayerScreen(
        albumArtUrl = Track.DEFAULT_COVER_URL,
        title = "Song Title",
        subtitle = "Artist Name",
        playPause = {},
        previous = {},
        next = {},
        isPlaying = false
    )
}

@Composable
fun PlayerView(
    viewModel: PlayerViewModel = hiltViewModel()
){
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()
    PlayerScreen(
        albumArtUrl = currentTrack?.cover ?: Track.DEFAULT_COVER_URL,
        title = currentTrack?.trackName ?: "Song Title",
        subtitle = currentTrack?.artist ?: "Artist Name",
        playPause = { viewModel.playOrPause() },
        previous = { viewModel.prev() },
        next = { viewModel.next() },
        isPlaying = isPlaying,
        shuffle = { viewModel.shuffle() },
        isShuffle = isShuffle,
        repeat = { viewModel.repeat() },
        isRepeat = isRepeat
        )
}

@Composable
fun PlayerScreen(
    albumArtUrl: String,
    title: String,
    subtitle: String,
    playPause: () -> Unit = {},
    previous: () -> Unit,
    next: () -> Unit = {},
    isPlaying: Boolean = false,
    shuffle: () -> Unit = {},
    isShuffle: Boolean = false,
    repeat: () -> Unit = {},
    isRepeat: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Album Art
        AsyncImage(
            model = albumArtUrl,
            contentDescription = "Album cover",
            modifier = Modifier
                .size(300.dp)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )

        // Title & Subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                textAlign = TextAlign.Center
            )
        }

        // Playback Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerControls(
                playPause = playPause,
                previous = previous,
                next = next,
                isPlaying = isPlaying,
                shuffle = shuffle,
                isShuffle = isShuffle,
                repeat = repeat,
                isRepeat = isRepeat
            )
        }
    }
}

@Composable
fun PlayerControls(
    playPause: () -> Unit = {},
    previous: () -> Unit,
    next: () -> Unit = {},
    isPlaying: Boolean = false,
    shuffle: () -> Unit = {},
    isShuffle: Boolean = false,
    repeat: () -> Unit = {},
    isRepeat: Boolean = false
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = LocalContentColor.current.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = shuffle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffle) activeColor else inactiveColor
            )
        }

        IconButton(onClick = previous) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous"
            )
        }

        IconButton(onClick = playPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }

        IconButton(onClick = next) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next"
            )
        }

        IconButton(onClick = repeat) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (isRepeat) activeColor else inactiveColor
            )
        }
    }
}