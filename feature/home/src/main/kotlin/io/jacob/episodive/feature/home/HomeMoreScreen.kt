package io.jacob.episodive.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.component.EpisodiveScaffold
import io.jacob.episodive.core.designsystem.component.SkeletonContainer
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.isRetryable
import io.jacob.episodive.core.ui.ChannelItem
import io.jacob.episodive.core.ui.ChannelItemSkeleton
import io.jacob.episodive.core.ui.EpisodeItem
import io.jacob.episodive.core.ui.EpisodeItemSkeleton
import io.jacob.episodive.core.ui.PodcastItem
import io.jacob.episodive.core.ui.PodcastItemSkeleton
import io.jacob.episodive.core.ui.asUiMessage
import io.jacob.episodive.core.ui.pagingAppendState
import io.jacob.episodive.core.ui.pagingRefreshState
import io.jacob.episodive.feature.home.navigation.HomeSection
import io.jacob.episodive.core.designsystem.R as designsystemR
import io.jacob.episodive.core.ui.R as uiR
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun HomeMoreRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeMoreViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPodcastClick: (Long) -> Unit,
    onChannelClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val unsavedMessage = stringResource(uiR.string.core_ui_snackbar_unsaved)
    val undoLabel = stringResource(uiR.string.core_ui_snackbar_undo)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeMoreEffect.NavigateBack -> onBackClick()
                is HomeMoreEffect.NavigateToPodcast -> onPodcastClick(effect.podcastId)
                is HomeMoreEffect.NavigateToChannel -> onChannelClick(effect.channelId)
                is HomeMoreEffect.ShowUnsaveSnackbar -> {
                    val undone = onShowSnackbar(unsavedMessage, undoLabel)
                    if (undone) {
                        viewModel.sendAction(HomeMoreAction.ToggleSavedEpisode(effect.episode))
                    }
                }
            }
        }
    }

    HomeMoreScreen(
        modifier = modifier.fillMaxSize(),
        section = viewModel.section,
        content = viewModel.content,
        onBackClick = { viewModel.sendAction(HomeMoreAction.ClickBack) },
        onPodcastClick = { viewModel.sendAction(HomeMoreAction.ClickPodcast(it)) },
        onChannelClick = { viewModel.sendAction(HomeMoreAction.ClickChannel(it)) },
        onPlayEpisode = { viewModel.sendAction(HomeMoreAction.PlayEpisode(it)) },
        onToggleLikedEpisode = { viewModel.sendAction(HomeMoreAction.ToggleLikedEpisode(it)) },
        onToggleSavedEpisode = { viewModel.sendAction(HomeMoreAction.ToggleSavedEpisode(it)) },
        onRetry = { viewModel.sendAction(HomeMoreAction.Retry) },
    )
}

/**
 * 홈 섹션의 전체 목록.
 *
 * 상세 화면들과 달리 [io.jacob.episodive.core.designsystem.component.FadeTopBarLayout] 을
 * 쓰지 않는다. 그쪽은 첫 항목이 큰 히어로라 제목을 늦게 띄워도 되지만, 여기는 첫 줄부터
 * 목록이라 "무엇의 목록인지"가 처음부터 보여야 한다.
 */
@Composable
internal fun HomeMoreScreen(
    modifier: Modifier = Modifier,
    section: HomeSection,
    content: HomeMoreContent,
    onBackClick: () -> Unit,
    onPodcastClick: (Long) -> Unit,
    onChannelClick: (Long) -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onToggleLikedEpisode: (Episode) -> Unit,
    onToggleSavedEpisode: (Episode) -> Unit,
    onRetry: () -> Unit,
) {
    EpisodiveScaffold(
        modifier = modifier,
        title = section.title(),
        navigationIcon = EpisodiveIcons.ArrowBack,
        navigationIconContentDescription = "Back",
        onNavigationClick = onBackClick,
    ) { paddingValues, nestedScrollConnection ->
        when (content) {
            is HomeMoreContent.PodcastPaging -> HomeMorePodcastGrid(
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                items = content.items.collectAsLazyPagingItems(),
                onPodcastClick = onPodcastClick,
            )

            is HomeMoreContent.EpisodePaging -> HomeMoreEpisodeList(
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                items = content.items.collectAsLazyPagingItems(),
                onPlayEpisode = onPlayEpisode,
                onToggleLikedEpisode = onToggleLikedEpisode,
                onToggleSavedEpisode = onToggleSavedEpisode,
            )

            is HomeMoreContent.ChannelList -> {
                val state by content.state.collectAsStateWithLifecycle()
                HomeMoreChannelGrid(
                    paddingValues = paddingValues,
                    nestedScrollConnection = nestedScrollConnection,
                    state = state,
                    onChannelClick = onChannelClick,
                    onRetry = onRetry,
                )
            }
        }
    }
}

/** 섹션 제목은 홈 헤더에 쓰는 것을 그대로 재사용한다 — 같은 목록에 다른 이름을 붙이지 않는다. */
@Composable
private fun HomeSection.title(): String = stringResource(
    when (this) {
        HomeSection.MyRecentPodcasts -> R.string.feature_home_section_my_recent_feeds
        HomeSection.RandomEpisodes -> R.string.feature_home_section_random_episodes
        HomeSection.MyTrendingPodcasts -> R.string.feature_home_section_my_trending_feeds
        HomeSection.FollowedPodcasts -> R.string.feature_home_section_followed_podcasts
        HomeSection.LocalTrendingPodcasts -> R.string.feature_home_section_trending_in_local
        HomeSection.ForeignTrendingPodcasts -> R.string.feature_home_section_trending_in_foreign
        HomeSection.LiveEpisodes -> R.string.feature_home_section_live_episodes
        HomeSection.Channels -> R.string.feature_home_section_channels
    }
)

@Composable
private fun HomeMorePodcastGrid(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    items: LazyPagingItems<Podcast>,
    onPodcastClick: (Long) -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        columns = GridCells.Fixed(HomeMoreGridColumns),
        contentPadding = PaddingValues(horizontal = dimension.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
        verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
    ) {
        pagingRefreshState(
            items = items,
            key = HomeMorePodcastPagingKey,
            // 한 화면 분량만 그린다. 실제로 올 개수보다 많이 그리면 목록이 채워지는 순간
            // 그만큼 줄어들며 스크롤이 튄다.
            loading = { HomeMorePodcastGridSkeleton() },
            empty = { HomeMoreMessage(text = stringResource(R.string.feature_home_more_empty)) },
            error = {
                HomeMoreMessage(
                    text = stringResource(R.string.feature_home_more_error),
                    onRetry = { items.retry() },
                )
            },
        )

        items(
            count = items.itemCount,
            key = { items.peek(it)?.id ?: it },
            contentType = { "podcast" },
        ) { index ->
            items[index]?.let { podcast ->
                PodcastItem(
                    // 캐러셀 기본 폭을 덮어 그리드 셀을 꽉 채운다.
                    modifier = Modifier.fillMaxWidth(),
                    podcast = podcast,
                    subtitle = podcast.ownerName.ifEmpty { podcast.author },
                    onClick = { onPodcastClick(podcast.id) },
                )
            }
        }

        pagingAppendState(
            items = items,
            key = HomeMorePodcastPagingKey,
            loading = { HomeMorePodcastGridSkeleton(rows = 1) },
        )

        item(span = { GridItemSpan(maxLineSpan) }) {
            // 미니플레이어가 떠 있는 만큼 비워야 마지막 카드가 가리지 않는다.
            Spacer(modifier = Modifier.height(dimension.playerBarSpace))
        }
    }
}

@Composable
private fun HomeMoreEpisodeList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    items: LazyPagingItems<Episode>,
    onPlayEpisode: (Episode) -> Unit,
    onToggleLikedEpisode: (Episode) -> Unit,
    onToggleSavedEpisode: (Episode) -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(dimension.listItemSpacing),
    ) {
        pagingRefreshState(
            items = items,
            key = HomeMoreEpisodePagingKey,
            loading = { HomeMoreEpisodeListSkeleton() },
            empty = { HomeMoreMessage(text = stringResource(R.string.feature_home_more_empty)) },
            error = {
                HomeMoreMessage(
                    text = stringResource(R.string.feature_home_more_error),
                    onRetry = { items.retry() },
                )
            },
        )

        items(
            count = items.itemCount,
            key = { items.peek(it)?.id ?: it },
            contentType = { "episode" },
        ) { index ->
            items[index]?.let { episode ->
                EpisodeItem(
                    modifier = Modifier.padding(horizontal = dimension.screenPadding),
                    episode = episode,
                    onClick = { onPlayEpisode(episode) },
                    onToggleLiked = { onToggleLikedEpisode(episode) },
                    onToggleSaved = { onToggleSavedEpisode(episode) },
                )
            }
        }

        pagingAppendState(
            items = items,
            key = HomeMoreEpisodePagingKey,
            loading = { HomeMoreEpisodeListSkeleton(count = 2) },
        )

        item {
            Spacer(modifier = Modifier.height(dimension.playerBarSpace))
        }
    }
}

@Composable
private fun HomeMoreChannelGrid(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    state: HomeMoreChannelState,
    onChannelClick: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    when (state) {
        is HomeMoreChannelState.Loading -> SkeletonContainer(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            HomeMoreChannelGridSkeleton()
        }

        is HomeMoreChannelState.Error -> ErrorScreen(
            modifier = modifier.padding(paddingValues),
            message = state.error.asUiMessage(),
            onRetry = if (state.error.isRetryable) onRetry else null,
        )

        is HomeMoreChannelState.Success -> LazyVerticalGrid(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(nestedScrollConnection),
            columns = GridCells.Fixed(HomeMoreGridColumns),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
            verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
        ) {
            if (state.channels.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HomeMoreMessage(text = stringResource(R.string.feature_home_more_empty))
                }
            }

            items(
                count = state.channels.size,
                key = { state.channels[it].id },
                contentType = { "channel" },
            ) { index ->
                val channel = state.channels[index]
                ChannelItem(
                    // 캐러셀 기본 폭을 덮어 그리드 셀을 꽉 채운다.
                    modifier = Modifier.fillMaxWidth(),
                    channel = channel,
                    onClick = { onChannelClick(channel.id) },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(dimension.playerBarSpace))
            }
        }
    }
}

/**
 * 목록이 비었거나 실패했을 때의 안내.
 *
 * 화면을 통째로 채우는 [ErrorScreen] 과 달리 Lazy 항목 안에 들어가므로 높이를 콘텐츠에
 * 맡긴다 — 항목 안에서 fillMaxSize 를 쓰면 높이가 0으로 접힌다.
 */
@Composable
private fun HomeMoreMessage(
    modifier: Modifier = Modifier,
    text: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = HomeMoreMessageHorizontalPadding,
                vertical = HomeMoreMessageVerticalPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Column(modifier = Modifier.padding(top = HomeMoreMessageActionSpacing)) {
                EpisodiveButton(onClick = onRetry) {
                    Text(text = stringResource(designsystemR.string.core_designsystem_retry))
                }
            }
        }
    }
}

@Composable
private fun HomeMorePodcastGridSkeleton(rows: Int = HomeMoreGridSkeletonRows) {
    val dimension = LocalDimensionTheme.current

    SkeletonContainer(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing)) {
            repeat(rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing)) {
                    repeat(HomeMoreGridColumns) {
                        PodcastItemSkeleton(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMoreEpisodeListSkeleton(count: Int = HomeMoreListSkeletonCount) {
    val dimension = LocalDimensionTheme.current

    SkeletonContainer(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = dimension.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimension.listItemSpacing),
        ) {
            repeat(count) {
                EpisodeItemSkeleton()
            }
        }
    }
}

@Composable
private fun HomeMoreChannelGridSkeleton(rows: Int = HomeMoreGridSkeletonRows) {
    val dimension = LocalDimensionTheme.current

    Column(
        modifier = Modifier.padding(horizontal = dimension.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
    ) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing)) {
                repeat(HomeMoreGridColumns) {
                    ChannelItemSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private const val HomeMoreGridColumns = 2

/** 로딩 자리 크기. 한 화면 분량만 그려 전환할 때 스크롤이 덜 튄다. */
private const val HomeMoreGridSkeletonRows = 3
private const val HomeMoreListSkeletonCount = 5

/**
 * 페이징 상태 아이템의 네임스페이스.
 *
 * 실제 항목 키(팟캐스트·에피소드 id)와 겹치지 않게 화면마다 고유한 이름을 쓴다.
 */
private const val HomeMorePodcastPagingKey = "homeMore:podcasts"
private const val HomeMoreEpisodePagingKey = "homeMore:episodes"

private val HomeMoreMessageHorizontalPadding = 32.dp
private val HomeMoreMessageVerticalPadding = 48.dp
private val HomeMoreMessageActionSpacing = 20.dp
