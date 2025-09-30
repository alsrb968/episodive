package io.jacob.episodive.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodiveIconButton
import io.jacob.episodive.core.designsystem.component.EpisodiveIconToggleButton
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.model.mapper.toIntSeconds
import io.jacob.episodive.core.model.mapper.toMediaTime
import io.jacob.episodive.core.testing.model.episodeTestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PlayerEffect.NavigateToPodcast -> {}
                is PlayerEffect.ShowPlayerBottomSheet -> {}
                is PlayerEffect.HidePlayerBottomSheet -> {
                    sheetState.hide()
                }
            }
        }
    }

    val s = state as? PlayerState.Success ?: return

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = {
            viewModel.sendAction(PlayerAction.CollapsePlayer)
        },
        sheetState = sheetState,
        dragHandle = null,
        scrimColor = Color.Transparent,
        contentWindowInsets = { WindowInsets(0) },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true,
        ),
    ) {
        PlayerScreen(
            modifier = Modifier
                .padding(WindowInsets.statusBars.asPaddingValues()),
            nowPlaying = s.nowPlaying,
            progress = s.progress,
            isPlaying = s.isPlaying,
            isFavorite = false,
//                dominantColor = s.dominantColor,
            actions = PlayerScreenActions(
                isFavorite = { flowOf(false)/*viewModel::isFavoriteTrack*/ },
                onFavorite = {},
                onFavoriteIndex = {},
                onPlayOrPause = { viewModel.sendAction(PlayerAction.PlayOrPause) },
                onPlayIndex = { viewModel.sendAction(PlayerAction.PlayIndex(it)) },
                onPrevious = { viewModel.sendAction(PlayerAction.Previous) },
                onNext = { viewModel.sendAction(PlayerAction.Next) },
                onShuffle = { viewModel.sendAction(PlayerAction.Shuffle) },
                onRepeat = { viewModel.sendAction(PlayerAction.Repeat) },
                onSeekTo = { viewModel.sendAction(PlayerAction.SeekTo(it)) },
                onSeekBackward = { viewModel.sendAction(PlayerAction.SeekBackward) },
                onSeekForward = { viewModel.sendAction(PlayerAction.SeekForward) },
            ),
            onCollapse = {
                scope.launch {
                    sheetState.hide()
                    viewModel.sendAction(PlayerAction.CollapsePlayer)
                }
            },
        )
    }
}


@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    nowPlaying: Episode,
    progress: Progress,
    isPlaying: Boolean,
    isFavorite: Boolean,
//    dominantColor: Color,
    actions: PlayerScreenActions,
    onCollapse: () -> Unit,
) {
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
//            .gradientBackground(
//                ratio = 1f,
//                startColor = dominantColor,
//                endColor = MaterialTheme.colorScheme.background
//            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ControlPanelTop(
                modifier = Modifier,
                onCollapse = onCollapse
            )

            Spacer(modifier = Modifier.height(20.dp))

            StateImage(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                imageUrl = nowPlaying.feedImage,
                contentDescription = nowPlaying.title
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(start = 4.dp),
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(end = 50.dp)
                        .basicMarquee(),
                    text = nowPlaying.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    onClick = actions.onFavorite,
                ) {
                    Icon(
                        imageVector = if (isFavorite) EpisodiveIcons.Favorite else EpisodiveIcons.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            ControlPanelProgress(
                modifier = Modifier
                    .fillMaxWidth(),
                nowPlaying = nowPlaying,
                progress = progress,
                actions = actions
            )

            ControlPanelBottom(
                modifier = Modifier
                    .fillMaxWidth(),
                isPlaying = isPlaying,
                actions = actions
            )

            Spacer(modifier = Modifier.weight(1f))
        }
//        Text(
//            text = nowPlaying.script,
//            style = MaterialTheme.typography.bodyMedium,
//        )

    }
}

data class PlayerScreenActions(
    val isFavorite: (String) -> Flow<Boolean>,
    val onFavorite: () -> Unit,
    val onFavoriteIndex: (Int) -> Unit,
    val onPlayOrPause: () -> Unit,
    val onPlayIndex: (Int) -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onShuffle: () -> Unit,
    val onRepeat: () -> Unit,
    val onSeekTo: (Long) -> Unit,
    val onSeekBackward: () -> Unit,
    val onSeekForward: () -> Unit
)

@Composable
fun ControlPanelTop(
    modifier: Modifier = Modifier,
    onCollapse: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        IconButton(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterStart),
            onClick = onCollapse
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = EpisodiveIcons.KeyboardArrowDown,
                contentDescription = "Down",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Artist Name",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ControlPanelProgress(
    modifier: Modifier = Modifier,
    nowPlaying: Episode,
    progress: Progress,
    actions: PlayerScreenActions,
) {
    var position by remember { mutableFloatStateOf(progress.positionRatio) }

    LaunchedEffect(progress.position) {
        position = progress.positionRatio
    }

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Slider(
            value = position,
            onValueChange = { value ->
                position = value
            },
            onValueChangeFinished = {
                actions.onSeekTo((position * progress.duration.toIntSeconds()).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            },
            track = { sliderState ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    Modifier
                        .fillMaxWidth(sliderState.value)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            }
        )

        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp),
            text = progress.position.toMediaTime(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp),
            text = progress.duration.toMediaTime(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

    }
}

@Composable
fun ControlPanelBottom(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    actions: PlayerScreenActions,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EpisodiveIconButton(
            onClick = actions.onSeekBackward,
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.Replay10,
                    contentDescription = "Replay",
                )
            }
        )

        EpisodiveIconButton(
            onClick = actions.onPrevious,
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.SkipPrevious,
                    contentDescription = "Previous",
                )
            }
        )

        EpisodiveIconToggleButton(
            checked = isPlaying,
            onCheckedChange = { actions.onPlayOrPause() },
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.PlayArrow,
                    contentDescription = "Play",
                )
            },
            checkedIcon = {
                Icon(
                    imageVector = EpisodiveIcons.Pause,
                    contentDescription = "Pause",
                )
            },
            colors = IconButtonDefaults.iconToggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.onBackground,
                checkedContentColor = MaterialTheme.colorScheme.background,
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            )
        )

        EpisodiveIconButton(
            onClick = actions.onNext,
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.SkipNext,
                    contentDescription = "Next",
                )
            }
        )

        EpisodiveIconButton(
            onClick = actions.onSeekForward,
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.Forward30,
                    contentDescription = "Forward",
                )
            }
        )
    }
}

@DevicePreviews
@Composable
private fun PlayerScreenPreview() {
    EpisodiveTheme {
        PlayerScreen(
            nowPlaying = episodeTestData,
            progress = Progress(1000.seconds, 2000.seconds, 3000.seconds),
            isPlaying = true,
            isFavorite = false,
//            dominantColor = Color.DarkGray,
            actions = PlayerScreenActions(
                isFavorite = { flowOf(false) },
                onFavorite = {},
                onFavoriteIndex = {},
                onPlayOrPause = {},
                onPlayIndex = {},
                onPrevious = {},
                onNext = {},
                onShuffle = {},
                onRepeat = {},
                onSeekTo = {},
                onSeekBackward = {},
                onSeekForward = {},
            ),
            onCollapse = {}
        )
    }
}