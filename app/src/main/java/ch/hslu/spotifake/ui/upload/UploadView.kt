package ch.hslu.spotifake.ui.upload

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UploadView(
    viewModel: UploadViewModel = hiltViewModel()
) {
    Text("Upload View!")
}