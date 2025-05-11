package ch.hslu.spotifake.ui.library

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import ch.hslu.spotifake.db.TrackDatabase

@Composable
fun LibraryView(
    db: TrackDatabase,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val stringList = db.trackDao().getAll()
    LazyColumn {
        items(
            count = stringList.size
        ) { index ->
            Text(stringList[index].trackName + ", " + stringList[index].artist)
        }
    }
}