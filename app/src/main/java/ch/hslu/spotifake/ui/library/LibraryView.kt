package ch.hslu.spotifake.ui.library

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LibraryView(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val tracks by viewModel.allItems.observeAsState(emptyList())

    LazyColumn {
        items(
            count = tracks.size
        ) { index ->
            Text(tracks[index].trackName + ", " + tracks[index].artist)
        }
    }
}