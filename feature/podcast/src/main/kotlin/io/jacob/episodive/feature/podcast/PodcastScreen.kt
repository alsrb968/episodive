package io.jacob.episodive.feature.podcast

import android.content.Intent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.component.EpisodiveButtonDefaults
import io.jacob.episodive.core.designsystem.component.EpisodiveIconButton
import io.jacob.episodive.core.designsystem.component.FadeTopBarLayout
import io.jacob.episodive.core.designsystem.component.HtmlTextContainer
import io.jacob.episodive.core.designsystem.component.SkeletonContainer
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.component.scrollbar.DraggableScrollbar
import io.jacob.episodive.core.designsystem.component.scrollbar.scrollbarState
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.screen.LoadingScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestData
import io.jacob.episodive.core.ui.EpisodeItem
import io.jacob.episodive.core.ui.EpisodeItemSkeleton
import io.jacob.episodive.core.ui.pagingAppendState
import io.jacob.episodive.core.ui.pagingRefreshState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import io.jacob.episodive.core.ui.R as uiR

/** 커버 팔레트가 준비되기 전 쓰는 히어로 그라디언트 시작색, 그리고 그라디언트가 깔리는 높이. */
private val PodcastHeaderGradientFallback = Color(0xFF6B2A20)
private val PodcastHeaderGradientHeight = 480.dp

private val PodcastHeaderTopPadding = 28.dp
private val PodcastHeaderCoverSize = 220.dp

/** 팔로우 버튼 폭 — 화면 폭을 다 채우면 지나치게 길쭉해 보인다. */
private val PodcastFollowButtonWidth = 190.dp

@Composable
internal fun PodcastRoute(
    modifier: Modifier = Modifier,
    viewModel: PodcastViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val followedMessage = stringResource(uiR.string.core_ui_snackbar_followed)
    val unfollowedMessage = stringResource(uiR.string.core_ui_snackbar_unfollowed)
    val unsavedMessage = stringResource(uiR.string.core_ui_snackbar_unsaved)
    val undoLabel = stringResource(uiR.string.core_ui_snackbar_undo)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PodcastEffect.ShowFollowSnackbar -> {
                    val message = if (effect.isFollowed) followedMessage else unfollowedMessage
                    val undone = onShowSnackbar(message, undoLabel)
                    if (undone) viewModel.sendAction(PodcastAction.ToggleFollowed)
                }
                is PodcastEffect.ShowUnsaveSnackbar -> {
                    val undone = onShowSnackbar(unsavedMessage, undoLabel)
                    if (undone) viewModel.sendAction(PodcastAction.ToggleSavedEpisode(effect.episode))
                }
            }
        }
    }

    when (val s = state) {
        is PodcastState.Loading -> LoadingScreen()

        is PodcastState.Success -> {
            PodcastScreen(
                modifier = modifier,
                podcast = s.podcast,
                episodes = viewModel.episodesPaging,
                onFollowClick = { viewModel.sendAction(PodcastAction.ToggleFollowed) },
                onEpisodeClick = { episode, visibleEpisodes ->
                    viewModel.sendAction(PodcastAction.PlayEpisode(episode, visibleEpisodes))
                },
                onToggleLikedEpisode = { viewModel.sendAction(PodcastAction.ToggleLikedEpisode(it)) },
                onToggleSavedEpisode = { viewModel.sendAction(PodcastAction.ToggleSavedEpisode(it)) },
                onBackClick = onBackClick,
                onShowSnackbar = onShowSnackbar
            )
        }

        is PodcastState.Error -> ErrorScreen(message = s.message)
    }
}

@Composable
internal fun PodcastScreen(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    episodes: Flow<PagingData<Episode>>,
    onFollowClick: () -> Unit,
    onEpisodeClick: (Episode, List<Episode>) -> Unit,
    onToggleLikedEpisode: (Episode) -> Unit,
    onToggleSavedEpisode: (Episode) -> Unit = {},
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val episodesPaging = episodes.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dimension = LocalDimensionTheme.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // 상단 그라디언트는 커버 아트에서 뽑은 색에서 시작해 화면 배경으로 흘러 내려간다.
    var dominantColor by remember { mutableStateOf(PodcastHeaderGradientFallback) }

    FadeTopBarLayout(
        modifier = modifier,
        state = listState,
        offset = 900,
        title = podcast.title,
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            // 커버가 상태바 시계와 겹치지 않도록 위를, 마지막 에피소드가 미니플레이어에
            // 가리지 않도록 아래를 띄운다.
            contentPadding = PaddingValues(
                top = statusBarTop,
                bottom = dimension.playerBarSpace,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PodcastHeader(
                    // 그라디언트를 리스트 뒤에 따로 깔지 않고 헤더 항목이 직접 그린다.
                    // 그래야 스크롤할 때 커버와 같이 위로 올라간다. 항목 높이와 무관하게
                    // 리스트 최상단(상태바 뒤)부터 정해진 높이만큼 칠하고, 뒤따르는 항목들이
                    // 그 위에 그려지므로 넘치는 부분이 콘텐츠를 가리지 않는다.
                    modifier = Modifier
                        .drawWithCache {
                            val topOffset = -statusBarTop.toPx()
                            val gradientHeight = PodcastHeaderGradientHeight.toPx()
                            val brush = Brush.verticalGradient(
                                0f to dominantColor,
                                0.46f to lerp(dominantColor, backgroundColor, 0.62f),
                                0.86f to backgroundColor,
                                startY = topOffset,
                                endY = topOffset + gradientHeight,
                            )
                            onDrawBehind {
                                drawRect(
                                    brush = brush,
                                    topLeft = Offset(0f, topOffset),
                                    size = Size(size.width, gradientHeight),
                                )
                            }
                        }
                        .padding(horizontal = dimension.screenPadding),
                    podcast = podcast,
                    onFollowClick = onFollowClick,
                    onDominantColorExtracted = { dominantColor = it },
                )
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = dimension.screenPadding,
                        end = dimension.screenPadding,
                        top = 20.dp,
                        bottom = 16.dp,
                    )
                )
            }

            item {
                Text(
                    modifier = Modifier.padding(
                        start = dimension.screenPadding,
                        end = dimension.screenPadding,
                        bottom = 14.dp,
                    ),
                    text = stringResource(R.string.feature_podcast_all_episodes_format).format(
                        podcast.episodeCount
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            pagingRefreshState(
                items = episodesPaging,
                key = "podcast:episodes",
                loading = {
                    // 팟캐스트 메타데이터의 episodeCount 로 상한을 둔다. 실제 로드될 줄 수보다
                    // 많이 그리면 목록이 채워지는 순간 그만큼 줄어들며 튄다.
                    // 에피소드가 몇 개 올지 알면 그만큼만 그려 전환할 때 덜 튄다. 다만 피드가
                    // episodeCount 를 0 으로 주는 경우가 있어 최소 한 줄은 남긴다 — 로딩 중에
                    // 아무것도 없으면 멈춘 화면으로 보인다.
                    EpisodeListSkeleton(count = podcast.episodeCount.coerceIn(1, 6))
                },
                empty = { EpisodesEmptyMessage() },
                error = { EpisodesErrorMessage() },
            )

            items(
                count = episodesPaging.itemCount,
                key = { episodesPaging.peek(it)?.id ?: it },
                contentType = { "episode" }
            ) { index ->
                episodesPaging[index]?.let { episode ->
                    EpisodeItem(
                        modifier = Modifier.padding(horizontal = dimension.screenPadding),
                        episode = episode,
                        onClick = {
                            val visibleEpisodes = episodesPaging.itemSnapshotList.items
                            onEpisodeClick(episode, visibleEpisodes)
                        },
                        onToggleLiked = { onToggleLikedEpisode(episode) },
                        onToggleSaved = { onToggleSavedEpisode(episode) },
                    )
                }
            }

            pagingAppendState(
                items = episodesPaging,
                key = "podcast:episodes",
                loading = { EpisodeListSkeleton(count = 2) },
            )

            item {
                Spacer(modifier = Modifier.height(dimension.playerBarHeight))
            }
        }

        listState.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .padding(top = 110.dp)
                .align(Alignment.TopEnd),
            state = listState.scrollbarState(itemsAvailable = episodesPaging.itemCount),
            orientation = Orientation.Vertical,
            onThumbMoved = { thumbPosition ->
                scope.launch {
                    val itemIndex = (thumbPosition * episodesPaging.itemCount).toInt()
                        .coerceIn(0, episodesPaging.itemCount - 1)
                    listState.scrollToItem(itemIndex)
                }
            }
        )
    }
}

/**
 * 에피소드 목록 로딩 자리. [count] 만큼 [EpisodeItemSkeleton] 을 쌓는다.
 *
 * 줄 간격·좌우 여백은 실제 에피소드 목록([EpisodeItem] 호출부)과 같은 값을 써야, 로딩이
 * 끝나고 실제 항목으로 바뀔 때 레이아웃이 튀지 않는다.
 */
@Composable
private fun EpisodeListSkeleton(modifier: Modifier = Modifier, count: Int) {
    val dimension = LocalDimensionTheme.current

    SkeletonContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimension.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(count) {
                EpisodeItemSkeleton()
            }
        }
    }
}

@Composable
private fun EpisodesEmptyMessage(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LocalDimensionTheme.current.screenPadding, vertical = 24.dp),
        text = stringResource(R.string.feature_podcast_episodes_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun EpisodesErrorMessage(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LocalDimensionTheme.current.screenPadding, vertical = 24.dp),
        text = stringResource(R.string.feature_podcast_episodes_error),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PodcastHeader(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    onFollowClick: () -> Unit,
    onDominantColorExtracted: (Color) -> Unit = {},
) {
    val isFollowed = podcast.isFollowed
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = PodcastHeaderTopPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StateImage(
            modifier = Modifier
                .size(PodcastHeaderCoverSize)
                .shadow(
                    elevation = 20.dp,
                    shape = EpisodiveShapes.heroCover,
                    ambientColor = Color.Black.copy(alpha = 0.7f),
                    spotColor = Color.Black.copy(alpha = 0.7f),
                )
                .clip(shape = EpisodiveShapes.heroCover),
            imageUrl = podcast.image,
            contentDescription = podcast.title,
            onDominantColorExtracted = onDominantColorExtracted,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = podcast.author,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = podcast.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        HtmlTextContainer(text = podcast.description) {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // 제목·저자는 중앙 정렬이지만 설명은 여러 줄로 흐르므로 중앙 정렬하면
                // 줄마다 좌우 끝이 들쭉날쭉해져 읽기 어렵다.
                textAlign = TextAlign.Start,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        ) {
            EpisodiveButton(
                // 옆의 공유 버튼과 같은 높이로 맞춘다. 높이가 다르면 두 버튼이 한 줄에
                // 있는데도 서로 어긋나 보인다. 폭은 화면을 다 채우지 않고 내용에 맞춰
                // 잡는다 — 화면 폭만큼 늘리면 알약이 지나치게 길쭉해진다.
                modifier = Modifier
                    .width(PodcastFollowButtonWidth)
                    .height(LocalDimensionTheme.current.buttonHeightCompact),
                onClick = onFollowClick,
                buttonColors = if (isFollowed) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    EpisodiveButtonDefaults.filledButtonColors()
                },
                text = { Text(stringResource(if (isFollowed) uiR.string.core_ui_unfollow else uiR.string.core_ui_follow)) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(19.dp),
                        imageVector = if (isFollowed) EpisodiveIcons.PersonRemove else EpisodiveIcons.PersonAdd,
                        contentDescription = null
                    )
                },
            )

            EpisodiveIconButton(
                modifier = Modifier.size(LocalDimensionTheme.current.buttonHeightCompact),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, podcast.link.ifEmpty { podcast.url })
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                },
                shape = EpisodiveShapes.pill,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                icon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = EpisodiveIcons.WorldShare,
                        contentDescription = "Share",
                    )
                },
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PodcastScreenPreview() {
    EpisodiveTheme {
        PodcastScreen(
            podcast = podcastTestData,
            episodes = flowOf(PagingData.from(episodeTestDataList)),
            onFollowClick = {},
            onEpisodeClick = { _, _ -> },
            onToggleLikedEpisode = {},
            onBackClick = {},
            onShowSnackbar = { _, _ -> false }
        )
    }
}