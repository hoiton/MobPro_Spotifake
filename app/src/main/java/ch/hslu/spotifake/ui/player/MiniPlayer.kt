import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.hslu.spotifake.db.Track
import ch.hslu.spotifake.ui.player.PlayerViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel = hiltViewModel(),
    onClick: () -> Unit
) {
    val track     by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    track?.let { t ->
        val scope = rememberCoroutineScope()
        val swipeThreshold = 80.dp
        val offsetX = remember { Animatable(0f) }

        val thresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // 1) Underlay icons
            Row(
                Modifier
                    .matchParentSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }

            // 2) Draggable foreground
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .matchParentSize()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                when {
                                    offsetX.value > thresholdPx -> {
                                        viewModel.prev()
                                    }
                                    offsetX.value < -thresholdPx -> {
                                        viewModel.next()
                                    }
                                }
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    )
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetX.snapTo(0f) }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                }
                            }
                        )
                    }
                    .clickable(onClick = onClick)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = t.cover ?: Track.DEFAULT_COVER_URL,
                        contentDescription = "Album cover",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = t.trackName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = t.artist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(onClick = { viewModel.playOrPause() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                }
            }
        }
    }
}
