package io.jacob.episodive.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.testing.model.episodeTestData
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.seconds

val PLAYER_BAR_HEIGHT = 70.dp

@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var isShowPlayer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PlayerEffect.NavigateToPodcast -> {}
                is PlayerEffect.ShowPlayerBottomSheet -> {
                    isShowPlayer = true
                }

                is PlayerEffect.HidePlayerBottomSheet -> {
                    isShowPlayer = false
                }
            }
        }
    }

//            val context = LocalContext.current
//            LaunchedEffect(s.nowPlaying.imageUrl) {
//                val color = extractDominantColorFromUrl(context, s.nowPlaying.imageUrl)
//                viewModel.sendIntent(PlayerUiIntent.SetDominantColor(color))
//            }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state is PlayerState.Ready,
            modifier = Modifier.align(Alignment.BottomCenter), // 위치 유지
            enter = slideInVertically(
                initialOffsetY = { it }, // 자기 키만큼 아래에서 등장
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            )
        ) {
            val s = state as? PlayerState.Ready ?: return@AnimatedVisibility

            PlayerBar(
                modifier = modifier,
                nowPlaying = s.content.nowPlaying,
                progress = s.content.progress,
                isPlaying = s.meta.isPlaying,
                isFavorite = false,
                actions = PlayerBarActions(
                    onPlayOrPause = { viewModel.sendAction(PlayerAction.PlayOrPause) },
                    onFavorite = {},
                ),
                onExpand = { viewModel.sendAction(PlayerAction.ExpandPlayer) },
            )
        }
    }

    if (isShowPlayer) {
        PlayerBottomSheet()
    }
}

@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    nowPlaying: Episode,
    progress: Progress,
    isPlaying: Boolean,
    isFavorite: Boolean,
    actions: PlayerBarActions,
    onExpand: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(PLAYER_BAR_HEIGHT)
            .padding(horizontal = 6.dp)
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onExpand
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    StateImage(
                        modifier = Modifier
                            .fillMaxSize(),
                        imageUrl = nowPlaying.feedImage,
                        contentDescription = nowPlaying.title
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(),
                        text = nowPlaying.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                ControlBar(
                    modifier = Modifier
                        .padding(end = 16.dp),
                    isFavorite = isFavorite,
                    isPlaying = isPlaying,
                    actions = actions,
                )
            }

            LinearProgressIndicator(
                progress = { progress.bufferedRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .padding(horizontal = 10.dp)
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.outlineVariant,
//                gapSize = 0.dp,
            )
            LinearProgressIndicator(
                progress = { progress.positionRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .padding(horizontal = 10.dp)
                    .align(Alignment.BottomCenter),
//                color = MaterialTheme.colorScheme.onSurface,
                trackColor = Color.Transparent,
//                gapSize = 0.dp,
            )
        }
    }
}

@Composable
fun ControlBar(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    isPlaying: Boolean,
    actions: PlayerBarActions,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        IconButton(
            modifier = Modifier
                .size(36.dp),
            onClick = actions.onFavorite,
        ) {
            Icon(
                imageVector = if (isFavorite) EpisodiveIcons.Favorite else EpisodiveIcons.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            modifier = Modifier
                .size(36.dp),
            onClick = actions.onPlayOrPause,
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp),
                imageVector = if (isPlaying) EpisodiveIcons.Pause else EpisodiveIcons.PlayArrow,
                contentDescription = "Play/Pause",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

data class PlayerBarActions(
    val onPlayOrPause: () -> Unit,
    val onFavorite: () -> Unit,
)

@DevicePreviews
@Composable
private fun PlayerBarPreview() {
    EpisodiveTheme {
        PlayerBar(
            nowPlaying = episodeTestData,
            progress = Progress(
                position = 30.seconds,
                buffered = 60.seconds,
                duration = 100.seconds,
            ),
            isPlaying = true,
            isFavorite = false,
            actions = PlayerBarActions(
                onPlayOrPause = {},
                onFavorite = {},
            ),
            onExpand = {},
        )
    }
}