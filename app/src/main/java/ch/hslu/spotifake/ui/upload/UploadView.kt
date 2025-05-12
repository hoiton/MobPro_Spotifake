package ch.hslu.spotifake.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage

@Composable
fun UploadView(
    viewModel: UploadViewModel = hiltViewModel()
) {
    val trackName by viewModel.trackName.collectAsState()
    val artistName by viewModel.artistName.collectAsState()
    val selectedFileName by viewModel.selectedFileName.collectAsState()
    val albumArtUrl by viewModel.selectedAlbumArtUrl.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onFileSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = albumArtUrl,
            contentDescription = "Album cover",
            modifier = Modifier
                .size(300.dp)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )

        TextField(
            value = trackName,
            onValueChange = viewModel::onTrackNameChange,
            label = { Text("Track Name") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = artistName,
            onValueChange = viewModel::onArtistNameChange,
            label = { Text("Artist Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { filePickerLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedFileName ?: "Select Music File"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = viewModel::uploadTrack,
            enabled = trackName.isNotBlank() && artistName.isNotBlank() && selectedFileName != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload")
        }
    }
}