package io.jacob.episodive.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import io.jacob.episodive.core.designsystem.component.EpisodiveGradientBackground
import io.jacob.episodive.core.designsystem.component.EpisodiveIconButton
import io.jacob.episodive.core.designsystem.component.EpisodiveIconToggleButton
import io.jacob.episodive.core.designsystem.component.EpisodiveTopAppBar
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.model.mapper.toLongMillis
import io.jacob.episodive.core.model.mapper.toMediaTime
import io.jacob.episodive.core.testing.model.episodeTestData
import kotlinx.coroutines.flow.collectLatest
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
            modifier = Modifier,
            nowPlaying = s.nowPlaying,
            progress = s.progress,
            isPlaying = s.isPlaying,
            isLike = false,
//                dominantColor = s.dominantColor,
            onCollapse = {
                scope.launch {
                    sheetState.hide()
                    viewModel.sendAction(PlayerAction.CollapsePlayer)
                }
            },
            onToggleLike = { /*viewModel.sendAction(PlayerAction.ToggleLike)*/ },
            onSeekTo = { viewModel.sendAction(PlayerAction.SeekTo(it)) },
            onPlayOrPause = { viewModel.sendAction(PlayerAction.PlayOrPause) },
            onBackward = { viewModel.sendAction(PlayerAction.SeekBackward) },
            onForward = { viewModel.sendAction(PlayerAction.SeekForward) },
            onPrevious = { viewModel.sendAction(PlayerAction.Previous) },
            onNext = { viewModel.sendAction(PlayerAction.Next) },
        )
    }
}


@Composable
private fun PlayerScreen(
    modifier: Modifier = Modifier,
    nowPlaying: Episode,
    progress: Progress,
    isPlaying: Boolean,
    isLike: Boolean,
//    dominantColor: Color,
    onCollapse: () -> Unit = {},
    onToggleLike: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onPlayOrPause: () -> Unit = {},
    onBackward: () -> Unit = {},
    onForward: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val listState = rememberLazyListState()

    EpisodiveGradientBackground {
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EpisodiveTopAppBar(
                title = {},
                navigationIcon = EpisodiveIcons.KeyboardArrowDown,
                navigationIconContentDescription = "Down",
                actionIcon = if (isLike) EpisodiveIcons.Favorite else EpisodiveIcons.FavoriteBorder,
                actionIconContentDescription = "Like",
                onNavigationClick = onCollapse,
                onActionClick = onToggleLike,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
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
                        .basicMarquee(),
                    text = nowPlaying.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }

            ControlPanelProgress(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                isPlaying = isPlaying,
                progress = progress,
                onSeekTo = onSeekTo
            )

            ControlPanelBottom(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(vertical = 38.dp),
                isPlaying = isPlaying,
                onPlayOrPause = onPlayOrPause,
                onBackward = onBackward,
                onForward = onForward,
                onPrevious = onPrevious,
                onNext = onNext,
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ControlPanelProgress(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    progress: Progress,
    onSeekTo: (Long) -> Unit = {},
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
                onSeekTo((position * progress.duration.toLongMillis()).toLong())
            },
            thumb = { sliderState ->
                val size = if (sliderState.isDragging) 24.dp else 16.dp
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.primary)
                )
            },
            track = { sliderState ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
                Box(
                    Modifier
                        .fillMaxWidth(progress.bufferedRatio)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Box(
                    Modifier
                        .fillMaxWidth(sliderState.value)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
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
private fun ControlPanelBottom(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPlayOrPause: () -> Unit = {},
    onBackward: () -> Unit = {},
    onForward: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EpisodiveIconButton(
            onClick = onBackward,
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = EpisodiveIcons.Replay10,
                    contentDescription = "Replay",
                )
            }
        )

        EpisodiveIconButton(
            onClick = onPrevious,
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = EpisodiveIcons.SkipPrevious,
                    contentDescription = "Previous",
                )
            }
        )

        EpisodiveIconToggleButton(
            modifier = Modifier.size(56.dp),
            checked = isPlaying,
            onCheckedChange = { onPlayOrPause() },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = EpisodiveIcons.PlayArrow,
                    contentDescription = "Play",
                )
            },
            checkedIcon = {
                Icon(
                    modifier = Modifier.size(32.dp),
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
            onClick = onNext,
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = EpisodiveIcons.SkipNext,
                    contentDescription = "Next",
                )
            }
        )

        EpisodiveIconButton(
            onClick = onForward,
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
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
            isLike = false,
//            dominantColor = Color.DarkGray,
        )
    }
}