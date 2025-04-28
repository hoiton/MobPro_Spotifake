package ch.hslu.spotifake.ui.library

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LibraryView(
     viewModel: LibraryViewModel = hiltViewModel()
) {
    Text("Library View!")
}