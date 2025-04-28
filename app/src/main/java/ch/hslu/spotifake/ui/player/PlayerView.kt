package ch.hslu.spotifake.ui.player

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlayerView(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    Text("Player View!")
}