package io.jacob.episodive.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import kotlin.math.ceil

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
        // 탭 루트의 오버사이즈 제목은 여기서 과하다 — 홈 섹션 헤더에서 눌러 들어온 화면이라
        // 그 헤더와 같은 크기로 읽혀야 어디서 왔는지가 이어진다.
        titleStyle = MaterialTheme.typography.titleMedium,
        // 한 종류의 항목만 길게 이어지는 화면이라, 제목은 어디에 있는지 확인할 때만 있으면
        // 된다. 내려가는 동안 비켜 주고 되올리면 돌아온다. 뒤로가기는 계속 남는다.
        hideTitleOnScroll = true,
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
                onRetry = onRetry,
            )

            is HomeMoreContent.EpisodePaging -> HomeMoreEpisodeList(
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                items = content.items.collectAsLazyPagingItems(),
                onPlayEpisode = onPlayEpisode,
                onToggleLikedEpisode = onToggleLikedEpisode,
                onToggleSavedEpisode = onToggleSavedEpisode,
                onRetry = onRetry,
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
    onRetry: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        columns = GridCells.Fixed(HomeMorePodcastGridColumns),
        contentPadding = paddingValues.asContentPadding(horizontal = dimension.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
        verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
    ) {
        pagingRefreshState(
            items = items,
            key = HomeMorePodcastPagingKey,
            // 행 수는 뷰포트에서 도출한다 — 아래를 덮지 못하면 다 불러온 빈 목록으로 읽힌다.
            loading = { HomeMorePodcastGridSkeleton() },
            empty = { HomeMoreMessage(text = stringResource(R.string.feature_home_more_empty)) },
            error = {
                HomeMoreMessage(
                    text = stringResource(R.string.feature_home_more_error),
                    onRetry = {
                        // 둘 다 부른다. items.retry() 는 PagingSource(로컬) 실패를 되돌리고,
                        // onRetry() 는 원격 실패로 흐름이 오류 상태로 바뀐 경우를 되돌린다.
                        // 후자는 Paging 자체 재시도가 닿지 않는다 — 흐름이 이미 끝나 있다.
                        items.retry()
                        onRetry()
                    },
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
    onRetry: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = paddingValues.asContentPadding(),
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
                    onRetry = {
                        // 둘 다 부른다. items.retry() 는 PagingSource(로컬) 실패를 되돌리고,
                        // onRetry() 는 원격 실패로 흐름이 오류 상태로 바뀐 경우를 되돌린다.
                        // 후자는 Paging 자체 재시도가 닿지 않는다 — 흐름이 이미 끝나 있다.
                        items.retry()
                        onRetry()
                    },
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
                .nestedScroll(nestedScrollConnection),
            columns = GridCells.Fixed(HomeMoreChannelGridColumns),
            contentPadding = paddingValues.asContentPadding(horizontal = dimension.screenPadding),
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
private fun HomeMorePodcastGridSkeleton(
    rows: Int = gridSkeletonRows(HomeMorePodcastGridColumns),
) {
    val dimension = LocalDimensionTheme.current

    SkeletonContainer(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing)) {
            repeat(rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing)) {
                    repeat(HomeMorePodcastGridColumns) {
                        PodcastItemSkeleton(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMoreEpisodeListSkeleton(count: Int = listSkeletonCount()) {
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
private fun HomeMoreChannelGridSkeleton(
    rows: Int = gridSkeletonRows(HomeMoreChannelGridColumns),
) {
    val dimension = LocalDimensionTheme.current

    Column(
        modifier = Modifier.padding(horizontal = dimension.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
    ) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing)) {
                repeat(HomeMoreChannelGridColumns) {
                    ChannelItemSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 팟캐스트 그리드 열 수.
 *
 * 3열이면 셀이 홈 캐러셀 카드와 비슷한 폭이 되어, 같은 콘텐츠가 화면만 바뀐 것으로 읽힌다.
 * 한 화면에 담기는 수도 늘어 훑어보기 좋다.
 */
private const val HomeMorePodcastGridColumns = 3

/**
 * 채널 그리드 열 수.
 *
 * 팟캐스트보다 적다. 채널 카드는 정사각 아트 아래 설명이 세 줄까지 들어가는 구조라
 * 폭이 좁아지면 그 설명부터 뭉개진다 — 커버만 보고도 알아보는 팟캐스트와 다르다.
 */
private const val HomeMoreChannelGridColumns = 2

/**
 * 스캐폴드가 준 여백을 목록의 contentPadding 으로 옮긴다.
 *
 * 컨테이너 패딩(`Modifier.padding`)이 아니어야 한다. 이 화면의 탑바는 콘텐츠 **위에**
 * 겹쳐 있어서, 컨테이너를 줄여 버리면 항목이 탑바 자리까지 올라가지 못한다 — 제목이
 * 사라져도 그 자리에 빈 띠가 남는다. contentPadding 이면 처음에는 탑바 아래에서
 * 시작하면서도 스크롤하면 항목이 그 자리를 지나 올라간다.
 */
@Composable
private fun PaddingValues.asContentPadding(horizontal: Dp = 0.dp): PaddingValues {
    // 스캐폴드가 준 좌우 여백을 버리지 않고 더한다. 세로에서는 0 이지만 가로에서는
    // 디스플레이 컷아웃과 내비게이션 바가 이쪽으로 와서, 버리면 항목이 그 밑에 깔린다.
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = horizontal + calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = horizontal + calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding(),
    )
}

/**
 * 뷰포트를 덮을 만큼의 그리드 스켈레톤 행 수.
 *
 * 상수로 못 박으면 열 수나 화면 크기가 바뀌는 순간 조용히 어긋난다 — 2열 기준으로 잡아 둔
 * 3행은 3열로 바꾸자 화면 절반만 덮었고, 나머지는 다 불러온 빈 목록처럼 보였다.
 *
 * 행 높이를 정사각 아트 폭으로만 어림한다. 그 아래 캡션 높이를 빼고 세므로 결과는 실제로
 * 필요한 것보다 항상 많다 — 모자라는 쪽으로는 틀리지 않는다. 남는 행은 뷰포트 밖으로
 * 잘려 보이지 않고, 스켈레톤은 목록 맨 위에서만 뜨므로 잘린 만큼 스크롤이 튀지도 않는다.
 */
@Composable
private fun gridSkeletonRows(columns: Int): Int {
    val dimension = LocalDimensionTheme.current
    val cellWidth = (viewportWidth() - dimension.screenPadding * 2 -
        dimension.gridSpacing * (columns - 1)) / columns

    return skeletonCount(itemHeight = cellWidth + dimension.gridSpacing)
}

/**
 * 뷰포트를 덮을 만큼의 리스트 스켈레톤 개수.
 *
 * 항목 높이를 썸네일 크기로 어림한다. 옆의 제목 두 줄과 메타 한 줄이 그보다 높으면
 * 높았지 낮지는 않으므로, 그리드와 같이 넉넉한 쪽으로 틀린다.
 */
@Composable
private fun listSkeletonCount(): Int {
    val dimension = LocalDimensionTheme.current

    return skeletonCount(itemHeight = dimension.thumbnailSmall + dimension.listItemSpacing)
}

@Composable
private fun skeletonCount(itemHeight: Dp): Int =
    ceil(viewportHeight() / itemHeight).toInt().coerceAtLeast(1)

@Composable
private fun viewportWidth(): Dp =
    with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }

@Composable
private fun viewportHeight(): Dp =
    with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }

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
