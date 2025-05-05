package ch.hslu.spotifake.ui.player

import android.os.Build
import android.widget.Toast
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Preview(showBackground = true)
@Composable
fun TestPlayerScreen() {
    PlayerScreen(
        albumArtUrl = "https://lastfm.freetls.fastly.net/i/u/770x0/0cc48bdf9e22bf52c4d91b9f66873319.jpg",
        title = "Song Title",
        subtitle = "Artist Name"
    )
}

@Composable
fun PlayerView(
    viewModel: PlayerViewModel = hiltViewModel()
){
    PlayerScreen(
        albumArtUrl = "https://lastfm.freetls.fastly.net/i/u/770x0/0cc48bdf9e22bf52c4d91b9f66873319.jpg",
        title = "Song Title",
        subtitle = "Artist Name"
    )
}

@Composable
fun PlayerScreen(
    albumArtUrl: String,
    title: String,
    subtitle: String,
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
            PlayerControls()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PlayerControls(
    viewModel: PlayerViewModel = hiltViewModel()
) {
//    val isPlaying by viewModel.isPlaying.collectAsState()
    // ToDo: can permission be requested and player be started in one action?
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.prev() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous"
            )
        }

        IconButton(onClick = {
            if (notificationPermissionState == null
                || notificationPermissionState.status.isGranted) {
                viewModel.playOrPause()
            } else {
                notificationPermissionState.launchPermissionRequest()
            }
        }) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            // ToDo: Change icon based on isPlaying state
//            if (isPlaying) {
//                Icon(Icons.Default.Clear, contentDescription = "Pause")
//            } else {
//                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
//            }
        }

        IconButton(onClick = { viewModel.next() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next"
            )
        }
    }
}