package io.jacob.episodive.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodiveScaffold
import io.jacob.episodive.core.designsystem.component.EpisodiveSearchBar
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.SkeletonBox
import io.jacob.episodive.core.designsystem.component.SkeletonContainer
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.component.scrollbar.DraggableScrollbar
import io.jacob.episodive.core.designsystem.component.scrollbar.scrollbarState
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.RecentSearch
import io.jacob.episodive.core.model.SearchResult
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.ui.EpisodeItem
import io.jacob.episodive.core.ui.EpisodesSection
import io.jacob.episodive.core.ui.EpisodesSectionSkeleton
import io.jacob.episodive.core.ui.PodcastsSection
import io.jacob.episodive.core.ui.PodcastsSectionSkeleton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onPodcastClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.NavigateToCategory -> {}
                is SearchEffect.NavigateToPodcast -> onPodcastClick(effect.podcastId)
                is SearchEffect.NavigateToEpisode -> {}
            }
        }
    }

    when (val s = state) {
        is SearchState.Loading -> SearchSkeleton()
        is SearchState.Success -> {
            SearchScreen(
                modifier = modifier,
                query = s.searchQuery,
                onQueryChange = { viewModel.sendAction(SearchAction.QueryChanged(it)) },
                onSearch = { viewModel.sendAction(SearchAction.ClickSearch(it)) },
                recentSearches = s.recentSearches,
                searchResult = s.searchResult,
                episodes = s.recentEpisodes,
                podcasts = s.trendingPodcasts,
                onPodcastClick = { viewModel.sendAction(SearchAction.ClickPodcast(it)) },
                onEpisodeClick = { viewModel.sendAction(SearchAction.ClickEpisode(it)) },
                onToggleLikedEpisode = { viewModel.sendAction(SearchAction.ToggleLikedEpisode(it)) },
                onRecentSearchClick = { viewModel.sendAction(SearchAction.ClickRecentSearch(it)) },
                onRemoveRecentSearch = { viewModel.sendAction(SearchAction.RemoveRecentSearch(it)) },
                onClearRecentSearches = { viewModel.sendAction(SearchAction.ClearRecentSearches) },
            )
        }

        is SearchState.Error -> ErrorScreen(message = s.message)
    }
}

/**
 * 검색 화면 로딩 자리. 제목은 실제 [EpisodiveScaffold] 로 그대로 그리고, 검색바·트렌딩
 * 팟캐스트·최근 에피소드 섹션 자리만 스켈레톤으로 채운다. 최근 검색 칩은 신규 사용자에게는
 * 아예 나타나지 않는 콘텐츠라 그리지 않는다 — 그리고 실제로 없으면 그 자체가 레이아웃 점프다.
 */
@Composable
private fun SearchSkeleton(modifier: Modifier = Modifier) {
    val dimension = LocalDimensionTheme.current

    EpisodiveScaffold(
        modifier = modifier,
        title = stringResource(R.string.feature_search_title),
    ) { paddingValues, _ ->
        SkeletonContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimension.screenPadding)
                        .height(dimension.fieldHeight),
                    shape = EpisodiveShapes.searchBar,
                )

                // 실제 EpisodiveSearchBar 접힘 상태의 bottom padding(18dp)과 같은 간격을
                // 둬, 로딩이 실제 콘텐츠로 바뀔 때 첫 섹션 위치가 튀지 않게 한다.
                Spacer(modifier = Modifier.height(18.dp))

                PodcastsSectionSkeleton(count = 3)

                HorizontalDivider(modifier = Modifier.padding(12.dp))

                EpisodesSectionSkeleton(count = 3)
            }
        }
    }
}

@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    recentSearches: List<RecentSearch>,
    searchResult: SearchResult,
    podcasts: List<Podcast>,
    episodes: List<Episode>,
    onPodcastClick: (Podcast) -> Unit = {},
    onEpisodeClick: (Episode) -> Unit = {},
    onToggleLikedEpisode: (Episode) -> Unit = {},
    onRecentSearchClick: (RecentSearch) -> Unit = {},
    onRemoveRecentSearch: (RecentSearch) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    isExpanded: Boolean = false,
) {
    EpisodiveScaffold(
        modifier = modifier,
        title = stringResource(R.string.feature_search_title),
    ) { paddingValues, nestedScrollConnection ->
        EpisodiveSearchBar(
            // 바깥 modifier 는 이미 EpisodiveScaffold 에 넘겼다. 여기서 다시 붙이면
            // 같은 modifier 가 두 번 적용된다.
            modifier = Modifier
                .padding(paddingValues),
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            isExpanded = isExpanded,
            placeholder = {
                Text(stringResource(R.string.feature_search_placeholder))
            },
            contentOnCollapse = {
                SearchContentsOnCollapse(
                    modifier = Modifier,
                    recentSearches = recentSearches,
                    episodes = episodes,
                    podcasts = podcasts,
                    onEpisodeClick = onEpisodeClick,
                    onToggleLikedEpisode = onToggleLikedEpisode,
                    onPodcastClick = onPodcastClick,
                    onRecentSearchClick = onRecentSearchClick,
                    onRemoveRecentSearch = onRemoveRecentSearch,
                )
            },
            contentOnExpand = { scrollState ->
                SearchResultsOnExpand(
                    scrollState = scrollState,
                    recentSearches = recentSearches,
                    searchResult = searchResult,
                    onPodcastClick = onPodcastClick,
                    onEpisodeClick = onEpisodeClick,
                    onToggleLikedEpisode = onToggleLikedEpisode,
                    onRecentSearchClick = onRecentSearchClick,
                    onRemoveRecentSearch = onRemoveRecentSearch,
                    onClearRecentSearches = onClearRecentSearches
                )
            }
        )
    }
}

@Composable
private fun SearchContentsOnCollapse(
    modifier: Modifier = Modifier,
    recentSearches: List<RecentSearch> = emptyList(),
    podcasts: List<Podcast>,
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit = {},
    onToggleLikedEpisode: (Episode) -> Unit = {},
    onPodcastClick: (Podcast) -> Unit = {},
    onRecentSearchClick: (RecentSearch) -> Unit = {},
    onRemoveRecentSearch: (RecentSearch) -> Unit = {},
) {
    val dimension = LocalDimensionTheme.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                RecentSearchChipsRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimension.screenPadding, vertical = 12.dp),
                    recentSearches = recentSearches,
                    onRecentSearchClick = onRecentSearchClick,
                    onRemoveRecentSearch = onRemoveRecentSearch,
                )
            }
        }

        if (podcasts.isNotEmpty()) {
            item {
                PodcastsSection(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = stringResource(R.string.feature_search_section_global_trending_feeds),
                    podcasts = podcasts,
                    subtitleProvider = { it.ownerName.ifEmpty { it.author } },
                    onPodcastClick = onPodcastClick
                )
            }
        }

        if (episodes.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(12.dp))
            }

            item {
                EpisodesSection(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = stringResource(R.string.feature_search_section_global_recent_episodes),
                    episodes = episodes,
                    onEpisodeClick = onEpisodeClick,
                    onToggleLikedEpisode = onToggleLikedEpisode,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(dimension.playerBarSpace))
        }
    }
}

@Composable
private fun RecentSearchChipsRow(
    modifier: Modifier = Modifier,
    recentSearches: List<RecentSearch>,
    onRecentSearchClick: (RecentSearch) -> Unit,
    onRemoveRecentSearch: (RecentSearch) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.feature_search_section_recent_searches),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            // 접힌 상태의 히스토리는 화면 앞머리를 차지하므로 줄 수를 묶는다.
            // 전체 목록은 검색창을 포커스하면 펼침 화면에서 볼 수 있다.
            maxLines = RecentSearchChipMaxLines,
        ) {
            recentSearches.forEach { recentSearch ->
                RecentSearchChip(
                    label = recentSearch.displayLabel(),
                    onClick = { onRecentSearchClick(recentSearch) },
                    onRemove = { onRemoveRecentSearch(recentSearch) },
                )
            }
        }
    }
}

@Composable
private fun RecentSearchChip(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(EpisodiveShapes.pill)
            // --card(#271E1A) = surfaceContainerHigh. surfaceContainer 는 바텀시트용이라
            // 한 단계 어두워, 같은 화면의 필터 칩과 미묘하게 다른 색이 된다 (원본 줄 237).
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = EpisodiveShapes.pill)
            .clickable { onClick() }
            .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(
            imageVector = EpisodiveIcons.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(15.dp)
                .clickable { onRemove() },
        )
    }
}

private fun RecentSearch.displayLabel(): String = when (this) {
    is RecentSearch.Query -> query
    is RecentSearch.PodcastSearch -> title
    is RecentSearch.EpisodeSearch -> title
}

@Composable
private fun SearchResultsOnExpand(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    recentSearches: List<RecentSearch>,
    searchResult: SearchResult,
    onPodcastClick: (Podcast) -> Unit = {},
    onEpisodeClick: (Episode) -> Unit = {},
    onToggleLikedEpisode: (Episode) -> Unit = {},
    onRecentSearchClick: (RecentSearch) -> Unit = {},
    onRemoveRecentSearch: (RecentSearch) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            state = scrollState
        ) {
            if (searchResult.podcasts.isNotEmpty()) {
                item {
                    PodcastsSection(
                        title = stringResource(R.string.feature_search_section_podcasts),
                        podcasts = searchResult.podcasts,
                        onMore = {},
                        onPodcastClick = onPodcastClick,
                    )
                }
            } else {
                item {
                    RecentSearchesSection(
                        title = stringResource(R.string.feature_search_section_recent_searches),
                        recentSearches = recentSearches,
                        onRecentSearchClicked = onRecentSearchClick,
                        onRemoveRecentSearch = onRemoveRecentSearch,
                        onClearRecentSearches = onClearRecentSearches
                    )
                }
            }

            if (searchResult.episodes.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(12.dp))
                }

                item {
                    SectionHeader(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = stringResource(R.string.feature_search_section_episodes),
                    )
                }

                items(
                    count = searchResult.episodes.size,
                    key = { searchResult.episodes[it].id },
                ) { index ->
                    val episode = searchResult.episodes[index]

                    EpisodeItem(
                        // 다른 리스트와 같은 화면 여백·항목 간격을 쓴다. 여기만 16/16 이면
                        // 결과 화면에서 좌측 정렬선이 어긋난다 (원본 줄 296).
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LocalDimensionTheme.current.screenPadding),
                        episode = episode,
                        onClick = { onEpisodeClick(episode) },
                        onToggleLiked = { onToggleLikedEpisode(episode) }
                    )

                    Spacer(modifier = Modifier.height(LocalDimensionTheme.current.listItemSpacing))
                }
            }

            item {
                Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
            }
        }

        scrollState.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .align(Alignment.TopEnd),
            state = scrollState.scrollbarState(itemsAvailable = searchResult.episodes.size),
            orientation = Orientation.Vertical,
            onThumbMoved = { thumbPosition ->
                scope.launch {
                    val itemIndex = (thumbPosition * searchResult.episodes.size).toInt()
                        .coerceIn(0, searchResult.episodes.size - 1)
                    scrollState.scrollToItem(itemIndex)
                }
            }
        )
    }
}

@Composable
private fun RecentSearchesSection(
    modifier: Modifier = Modifier,
    title: String,
    recentSearches: List<RecentSearch>,
    onRecentSearchClicked: (RecentSearch) -> Unit,
    onRemoveRecentSearch: (RecentSearch) -> Unit,
    onClearRecentSearches: () -> Unit
) {
    val dimension = LocalDimensionTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimension.headerPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.feature_search_clear_recent_searches),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                // v1 은 아이콘 버튼이라 contentDescription 이 있었다. 텍스트 링크로 바뀌면서
                // 스크린리더가 읽을 라벨이 사라졌으므로 명시적으로 붙인다.
                modifier = Modifier
                    .clickable { onClearRecentSearches() }
                    .semantics { contentDescription = SearchClearAllContentDescription },
            )
        }

        Column {
            recentSearches.forEach { recentSearch ->
                RecentSearchItem(
                    recentSearch = recentSearch,
                    onClick = { onRecentSearchClicked(recentSearch) },
                    onRemove = { onRemoveRecentSearch(recentSearch) }
                )
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    modifier: Modifier = Modifier,
    recentSearch: RecentSearch,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = dimension.headerPadding, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (recentSearch) {
            is RecentSearch.Query -> {
                Icon(
                    imageVector = EpisodiveIcons.History,
                    contentDescription = "Recent Search Icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = recentSearch.query,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            is RecentSearch.PodcastSearch -> {
                StateImage(
                    imageUrl = recentSearch.imageUrl,
                    contentDescription = recentSearch.title,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recentSearch.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (recentSearch.author.isNotEmpty()) {
                            stringResource(R.string.feature_search_recent_podcast_subtitle, recentSearch.author)
                        } else {
                            stringResource(R.string.feature_search_recent_podcast_subtitle_no_author)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            is RecentSearch.EpisodeSearch -> {
                StateImage(
                    imageUrl = recentSearch.imageUrl,
                    contentDescription = recentSearch.title,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(EpisodeHistoryCoverShape),
                    contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recentSearch.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (recentSearch.feedTitle.isNotEmpty()) {
                            stringResource(R.string.feature_search_recent_episode_subtitle, recentSearch.feedTitle)
                        } else {
                            stringResource(R.string.feature_search_recent_episode_subtitle_no_feed)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Icon(
            imageVector = EpisodiveIcons.Close,
            contentDescription = "Remove Recent Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(17.dp)
                .clickable { onRemove() },
        )
    }
}

/** 접힌 검색 화면에서 히스토리 칩이 흐를 수 있는 최대 줄 수. */
private const val RecentSearchChipMaxLines = 5

private const val SearchClearAllContentDescription = "Clear recent searches"

// 에피소드 히스토리 커버 반경(9dp) — 팟캐스트(12dp=MaterialTheme.shapes.small)와 달리 토큰에 없는 값.
private val EpisodeHistoryCoverShape = RoundedCornerShape(9.dp)

@DevicePreviews
@Composable
private fun SearchScreenOnCollapsePreview() {
    EpisodiveTheme {
        SearchScreen(
            query = "test",
            onQueryChange = {},
            onSearch = {},
            recentSearches = emptyList(),
            searchResult = SearchResult(
                podcasts = podcastTestDataList.take(3),
                episodes = episodeTestDataList,
            ),
            podcasts = podcastTestDataList,
            episodes = episodeTestDataList,
        )
    }
}

@DevicePreviews
@Composable
private fun SearchScreenOnExpandPreview() {
    EpisodiveTheme {
        SearchScreen(
            query = "test",
            onQueryChange = {},
            onSearch = {},
            recentSearches = emptyList(),
            searchResult = SearchResult(
                podcasts = podcastTestDataList.take(3),
                episodes = episodeTestDataList,
            ),
            podcasts = podcastTestDataList,
            episodes = episodeTestDataList,
            isExpanded = true,
        )
    }
}

@DevicePreviews
@Composable
private fun SearchScreenOnExpandRecentSearchPreview() {
    EpisodiveTheme {
        SearchScreen(
            query = "test",
            onQueryChange = {},
            onSearch = {},
            recentSearches = listOf(
                RecentSearch.Query(
                    id = 1,
                    query = "개발 팟캐스트",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
                RecentSearch.PodcastSearch(
                    id = 2,
                    podcastId = 100,
                    title = "코틀린 라디오",
                    imageUrl = "",
                    author = "JetBrains",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
                RecentSearch.EpisodeSearch(
                    id = 3,
                    episodeId = 200,
                    title = "Compose UI 완전 정복 #42",
                    imageUrl = "",
                    feedTitle = "Android Developers",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
                RecentSearch.Query(
                    id = 4,
                    query = "machine learning",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
                RecentSearch.PodcastSearch(
                    id = 5,
                    podcastId = 101,
                    title = "The Changelog",
                    imageUrl = "",
                    author = "Changelog Media",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(
                podcasts = emptyList(),
                episodes = emptyList(),
            ),
            podcasts = podcastTestDataList,
            episodes = episodeTestDataList,
            isExpanded = true,
        )
    }
}

@DevicePreviews
@Composable
private fun SearchSkeletonPreview() {
    EpisodiveTheme {
        SearchSkeleton()
    }
}