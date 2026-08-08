package io.jacob.episodive.feature.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.jacob.episodive.core.designsystem.component.EpisodiveChipDefaults
import io.jacob.episodive.core.designsystem.component.EpisodiveFilterChip
import io.jacob.episodive.core.designsystem.component.EpisodiveScaffold
import io.jacob.episodive.core.designsystem.component.EpisodiveSearchBar
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.SectionHeaderSkeleton
import io.jacob.episodive.core.designsystem.component.SkeletonBox
import io.jacob.episodive.core.designsystem.component.SkeletonContainer
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.model.isRetryable
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.ui.R as uiR
import io.jacob.episodive.core.ui.CategoryItem
import io.jacob.episodive.core.ui.EpisodeDetailItem
import io.jacob.episodive.core.ui.EpisodeDetailItemSkeleton
import io.jacob.episodive.core.ui.EpisodeItem
import io.jacob.episodive.core.ui.EpisodeItemSkeleton
import io.jacob.episodive.core.ui.PlayedEpisodeItem
import io.jacob.episodive.core.ui.PlayedEpisodeItemSkeleton
import io.jacob.episodive.core.ui.PodcastDetailItem
import io.jacob.episodive.core.ui.PodcastDetailItemSkeleton
import io.jacob.episodive.core.ui.PodcastsSection
import io.jacob.episodive.core.ui.PodcastsSectionSkeleton
import io.jacob.episodive.core.ui.asUiMessage
import io.jacob.episodive.core.ui.displayName
import io.jacob.episodive.core.ui.pagingAppendState
import io.jacob.episodive.core.ui.pagingRefreshState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
    onPodcastClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val unsavedMessage = stringResource(uiR.string.core_ui_snackbar_unsaved)
    val undoLabel = stringResource(uiR.string.core_ui_snackbar_undo)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.NavigateToPodcast -> onPodcastClick(effect.podcastId)
                is LibraryEffect.ShowUnsaveSnackbar -> {
                    val undone = onShowSnackbar(unsavedMessage, undoLabel)
                    if (undone) viewModel.sendAction(LibraryAction.ToggleSavedEpisode(effect.episode))
                }
            }
        }
    }

    when (val s = state) {
        is LibraryState.Loading -> LibrarySkeleton(modifier = modifier)

        is LibraryState.Success -> LibraryScreen(
            modifier = modifier,
            query = s.findQuery,
            onQueryChange = { viewModel.sendAction(LibraryAction.QueryChanged(it)) },
            onFind = { viewModel.sendAction(LibraryAction.ClickFind(it)) },
            section = s.section,
            onSectionChange = { viewModel.sendAction(LibraryAction.SelectSection(it)) },
            playedEpisodes = s.allPlayedEpisodes,
            likedEpisodes = s.likedEpisodes,
            savedEpisodes = s.savedEpisodes,
            followedPodcasts = s.followedPodcasts,
            preferredCategories = s.preferredCategories,
            selectableCategories = s.selectableCategories,
            playedEpisodesPaging = viewModel.playedEpisodesPaging,
            likedEpisodesPaging = viewModel.likedEpisodesPaging,
            savedEpisodesPaging = viewModel.savedEpisodesPaging,
            followedPodcastsPaging = viewModel.followedPodcastsPaging,
            onPlayedEpisodeClick = { viewModel.sendAction(LibraryAction.ClickPlayingEpisode(it)) },
            onEpisodeClick = { viewModel.sendAction(LibraryAction.ClickEpisode(it)) },
            onPodcastClick = { viewModel.sendAction(LibraryAction.ClickPodcast(it)) },
            onToggleLikedEpisode = { viewModel.sendAction(LibraryAction.ToggleLikedEpisode(it)) },
            onToggleSavedEpisode = { viewModel.sendAction(LibraryAction.ToggleSavedEpisode(it)) },
            onToggleFollowedPodcast = { viewModel.sendAction(LibraryAction.ToggleFollowedPodcast(it)) },
            onTogglePreferredCategory = {
                viewModel.sendAction(
                    LibraryAction.TogglePreferredCategory(
                        it
                    )
                )
            },
        )

        is LibraryState.Error -> ErrorScreen(
            message = s.error.asUiMessage(),
            // 재시도해도 같은 결과가 오는 실패(없는 대상, 인증 실패)에는 버튼을 감춘다.
            onRetry = if (s.error.isRetryable) {
                { viewModel.sendAction(LibraryAction.Retry) }
            } else {
                null
            },
        )
    }
}

/**
 * 보관함 화면 로딩 자리. 제목은 실제 [EpisodiveScaffold] 로 그대로 그리고, 탭 칩 자리와
 * "전체" 탭의 대표 섹션 3개(최근 들은 에피소드·좋아요 표시한 에피소드·팔로우한 팟캐스트)
 * 자리만 스켈레톤으로 채운다. 섹션 3개면 뷰포트를 채우므로 나머지 섹션은 그리지 않는다.
 */
@Composable
private fun LibrarySkeleton(modifier: Modifier = Modifier) {
    val dimension = LocalDimensionTheme.current

    EpisodiveScaffold(
        modifier = modifier,
        title = stringResource(R.string.feature_library_title),
        subTitle = {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                // 실제 SectionFilter 처럼 스크롤 가능한 자리지만, 로딩 중에 끌리면 없는
                // 콘텐츠를 만지는 것처럼 보인다. 스크롤을 막아 "곧 채워질 자리"로만 읽히게 한다.
                userScrollEnabled = false,
                // 원본은 칩 행 아래 16px 을 띄운다(SectionFilter 와 동일, 원본 줄 748).
                contentPadding = PaddingValues(
                    start = dimension.screenPadding,
                    end = dimension.screenPadding,
                    bottom = 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(dimension.chipSpacing),
            ) {
                items(LibraryFilterChipSkeletonWidths) { width ->
                    SkeletonBox(
                        modifier = Modifier
                            .height(LibraryFilterChipSkeletonHeight)
                            .width(width),
                        shape = EpisodiveChipDefaults.PillShape,
                    )
                }
            }
        },
    ) { paddingValues, _ ->
        SkeletonContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(LibrarySectionSpacing),
            ) {
                Column {
                    // 실제 섹션에는 더 보기가 붙는다. 여기서 자리를 안 잡으면 전환 때 아래가 밀린다.
                    SectionHeaderSkeleton(hasAction = true)

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
                        contentPadding = PaddingValues(horizontal = dimension.screenPadding),
                    ) {
                        items(2) {
                            PlayedEpisodeItemSkeleton(modifier = Modifier.width(250.dp))
                        }
                    }
                }

                Column {
                    // 실제 섹션에는 더 보기가 붙는다. 여기서 자리를 안 잡으면 전환 때 아래가 밀린다.
                    SectionHeaderSkeleton(hasAction = true)

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
                        contentPadding = PaddingValues(horizontal = dimension.screenPadding),
                    ) {
                        items(2) {
                            EpisodeDetailItemSkeleton()
                        }
                    }
                }

                PodcastsSectionSkeleton(count = 3)
            }
        }
    }
}

/** 탭 칩 6개 자리의 폭. 실제 라벨 길이가 제각각인 것처럼 살짝씩 다르게 준다. */
private val LibraryFilterChipSkeletonWidths = listOf(48.dp, 64.dp, 56.dp, 52.dp, 62.dp, 58.dp)

/** M3 FilterChip 기본 높이. */
private val LibraryFilterChipSkeletonHeight = 32.dp

@Composable
internal fun LibraryScreen(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onFind: (String) -> Unit,
    section: LibrarySection,
    onSectionChange: (LibrarySection) -> Unit = {},
    playedEpisodes: List<Episode>,
    likedEpisodes: List<Episode>,
    savedEpisodes: List<Episode>,
    followedPodcasts: List<Podcast>,
    preferredCategories: List<Category>,
    selectableCategories: List<SelectableCategory>,
    playedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>>,
    likedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>>,
    savedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>>,
    followedPodcastsPaging: Flow<PagingData<SeparatedUiModel<Podcast>>>,
    onPlayedEpisodeClick: (Episode) -> Unit = {},
    onEpisodeClick: (Episode) -> Unit = {},
    onPodcastClick: (Podcast) -> Unit = {},
    onToggleLikedEpisode: (Episode) -> Unit = {},
    onToggleSavedEpisode: (Episode) -> Unit = {},
    onToggleFollowedPodcast: (Podcast) -> Unit = {},
    onTogglePreferredCategory: (Category) -> Unit = {},
) {
    var showFind by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    // 더 보기로 들어온 탭은 화면이 아니라 필터라, 놔두면 뒤로가기가 보관함을 통째로
    // 벗어난다. 사용자가 방금 좁힌 것은 목록이므로 되돌릴 것도 목록이다 — '모든' 으로
    // 돌아온 뒤에야 뒤로가기가 화면을 떠난다.
    BackHandler(enabled = showFind || section != LibrarySection.All) {
        // 검색창이 먼저다. 상단 액션이 검색창을 열 때 섹션을 All 로 되돌리므로, 이 조건이
        // 없으면 검색창이 떠 있는 동안 BackHandler 가 아예 꺼져 뒤로가기가 보관함 탭을
        // 통째로 벗어난다 — 사용자는 검색창만 닫히길 기대한다.
        if (showFind) showFind = false else onSectionChange(LibrarySection.All)
    }

    EpisodiveScaffold(
        modifier = modifier,
        title = stringResource(R.string.feature_library_title),
        subTitle = {
            FindOrFilter(
                scrollState = scrollState,
                showFind = showFind,
                query = query,
                onQueryChange = onQueryChange,
                onFind = onFind,
                section = section,
                onSectionChange = onSectionChange
            )
        },
        actionIcon = if (showFind) EpisodiveIcons.Close else EpisodiveIcons.Search,
        actionIconContentDescription = "search",
        onActionClick = {
            showFind = !showFind
            if (showFind) {
                onSectionChange(LibrarySection.All)
            }
        }
    ) { paddingValues, nestedScrollConnection ->
        when (section) {
            LibrarySection.All -> AllSectionContent(
                modifier = modifier,
                paddingValues = paddingValues,
                scrollState = scrollState,
                nestedScrollConnection = nestedScrollConnection,
                playedEpisodes = playedEpisodes,
                likedEpisodes = likedEpisodes,
                savedEpisodes = savedEpisodes,
                followedPodcasts = followedPodcasts,
                preferredCategories = preferredCategories,
                onPlayedEpisodeClick = onPlayedEpisodeClick,
                onEpisodeClick = onEpisodeClick,
                onPodcastClick = onPodcastClick,
                onSectionMore = onSectionChange,
            )

            LibrarySection.RecentlyListened -> RecentlyListenedContent(
                modifier = modifier,
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                playedEpisodesPaging = playedEpisodesPaging,
                onPlayedEpisodeClick = onPlayedEpisodeClick
            )

            LibrarySection.Liked -> LikedContent(
                modifier = modifier,
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                likedEpisodesPaging = likedEpisodesPaging,
                onLikedEpisodeClick = { onEpisodeClick(it) },
                onToggleLiked = onToggleLikedEpisode
            )

            LibrarySection.Saved -> SavedContent(
                modifier = modifier,
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                savedEpisodesPaging = savedEpisodesPaging,
                onSavedEpisodeClick = { onEpisodeClick(it) },
                onToggleSaved = onToggleSavedEpisode
            )

            LibrarySection.Followed -> FollowedContent(
                modifier = modifier,
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                followedPodcastsPaging = followedPodcastsPaging,
                onFollowedPodcastClick = { onPodcastClick(it) },
                onToggleFollowed = onToggleFollowedPodcast
            )

            LibrarySection.Preferred -> PreferredContent(
                modifier = modifier,
                paddingValues = paddingValues,
                nestedScrollConnection = nestedScrollConnection,
                selectableCategories = selectableCategories,
                onCategoryClick = {},
                onTogglePreferred = onTogglePreferredCategory
            )
        }
    }
}

/** 보관함 "전체" 탭에서 섹션과 섹션 사이 간격. */
private val LibrarySectionSpacing = 30.dp

@Composable
private fun AllSectionContent(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    playedEpisodes: List<Episode>,
    likedEpisodes: List<Episode>,
    savedEpisodes: List<Episode>,
    followedPodcasts: List<Podcast>,
    preferredCategories: List<Category>,
    onPlayedEpisodeClick: (Episode) -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onPodcastClick: (Podcast) -> Unit,
    /** 섹션 헤더의 더 보기. 새 화면으로 가지 않고 상단 필터를 그 탭으로 옮긴다. */
    onSectionMore: (LibrarySection) -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                // 섹션 사이는 리스트 항목 사이보다 확실히 넓어야 한다. 16dp 로 두면
                // 앞 캐러셀 카드와 다음 섹션 제목이 붙어 어디서 끊기는지 읽히지 않는다.
                verticalArrangement = Arrangement.spacedBy(LibrarySectionSpacing),
            ) {
                if (playedEpisodes.isNotEmpty()) {
                    PlayedEpisodeRowSection(
                        title = stringResource(R.string.feature_library_section_recently_listened_episodes),
                        playedEpisodes = playedEpisodes,
                        onPlayedEpisodeClick = onPlayedEpisodeClick,
                        onMore = { onSectionMore(LibrarySection.RecentlyListened) },
                    )
                }

                if (likedEpisodes.isNotEmpty()) {
                    EpisodeRowSection(
                        title = stringResource(R.string.feature_library_section_liked_episodes),
                        episodes = likedEpisodes,
                        onEpisodeClick = onEpisodeClick,
                        onMore = { onSectionMore(LibrarySection.Liked) },
                    )
                }

                if (savedEpisodes.isNotEmpty()) {
                    EpisodeRowSection(
                        title = stringResource(R.string.feature_library_section_saved_episodes),
                        episodes = savedEpisodes,
                        onEpisodeClick = onEpisodeClick,
                        onMore = { onSectionMore(LibrarySection.Saved) },
                    )
                }

                if (followedPodcasts.isNotEmpty()) {
                    PodcastsSection(
                        title = stringResource(R.string.feature_library_section_followed_podcasts),
                        podcasts = followedPodcasts,
                        onPodcastClick = onPodcastClick,
                        onMore = { onSectionMore(LibrarySection.Followed) },
                    )
                }

                if (preferredCategories.isNotEmpty()) {
                    CategorySection(
                        title = stringResource(R.string.feature_library_section_preferred_categories),
                        categories = preferredCategories,
                        onCategoryClick = {},
                        onMore = { onSectionMore(LibrarySection.Preferred) },
                    )
                }

                if (
                    playedEpisodes.isEmpty() &&
                    likedEpisodes.isEmpty() &&
                    savedEpisodes.isEmpty() &&
                    followedPodcasts.isEmpty() &&
                    preferredCategories.isEmpty()
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimension.screenPadding),
                        text = stringResource(R.string.feature_library_not_found_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
}

@Composable
private fun RecentlyListenedContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    playedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>>,
    onPlayedEpisodeClick: (Episode) -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val items = playedEpisodesPaging.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        pagingRefreshState(
            items = items,
            key = "library:played",
            loading = { PlayedEpisodeListSkeleton(count = 5) },
            empty = { LibraryEmptyMessage(text = stringResource(R.string.feature_library_recently_listened_empty)) },
            error = { LibraryLoadErrorMessage() },
        )

        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                // peek 은 get 과 달리 페이지 로드를 부르지 않는다. enablePlaceholders=false 라
                // 이 범위에서 null 은 오지 않지만, 단정 대신 기본값으로 흘려보내 크래시 여지를
                // 남기지 않는다.
                if (items.peek(it) is SeparatedUiModel.Separator) "separator" else "episode"
            }
        ) { index ->
            when (val item = items[index] ?: return@items) {
                is SeparatedUiModel.Content -> {
                    val episode = item.data
                    PlayedEpisodeItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimension.screenPadding)
                            .animateItem(),
                        playedEpisode = episode,
                        onClick = { onPlayedEpisodeClick(episode) },
                    )
                }

                is SeparatedUiModel.Separator -> {
                    val date = item.label
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = dimension.screenPadding)
                            .padding(vertical = 8.dp),
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        pagingAppendState(
            items = items,
            key = "library:played",
            loading = { PlayedEpisodeListSkeleton(count = 2) },
        )

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
}

@Composable
private fun LikedContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    likedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>>,
    onLikedEpisodeClick: (Episode) -> Unit,
    onToggleLiked: (Episode) -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val items = likedEpisodesPaging.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        pagingRefreshState(
            items = items,
            key = "library:liked",
            loading = { LibraryEpisodeListSkeleton(count = 6) },
            empty = { LibraryEmptyMessage(text = stringResource(R.string.feature_library_liked_empty)) },
            error = { LibraryLoadErrorMessage() },
        )

        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                // peek 은 get 과 달리 페이지 로드를 부르지 않는다. enablePlaceholders=false 라
                // 이 범위에서 null 은 오지 않지만, 단정 대신 기본값으로 흘려보내 크래시 여지를
                // 남기지 않는다.
                if (items.peek(it) is SeparatedUiModel.Separator) "separator" else "episode"
            }
        ) { index ->
            when (val item = items[index] ?: return@items) {
                is SeparatedUiModel.Content -> {
                    val episode = item.data
                    EpisodeItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimension.screenPadding)
                            .animateItem(),
                        episode = episode,
                        onClick = { onLikedEpisodeClick(episode) },
                        onToggleLiked = { onToggleLiked(episode) }
                    )
                }

                is SeparatedUiModel.Separator -> {
                    val date = item.label
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = dimension.screenPadding)
                            .padding(vertical = 8.dp),
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        pagingAppendState(
            items = items,
            key = "library:liked",
            loading = { LibraryEpisodeListSkeleton(count = 2) },
        )

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
}

@Composable
private fun SavedContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    savedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>>,
    onSavedEpisodeClick: (Episode) -> Unit,
    onToggleSaved: (Episode) -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val items = savedEpisodesPaging.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        pagingRefreshState(
            items = items,
            key = "library:saved",
            loading = { LibraryEpisodeListSkeleton(count = 6) },
            empty = { LibraryEmptyMessage(text = stringResource(R.string.feature_library_saved_empty)) },
            error = { LibraryLoadErrorMessage() },
        )

        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                // peek 은 get 과 달리 페이지 로드를 부르지 않는다. enablePlaceholders=false 라
                // 이 범위에서 null 은 오지 않지만, 단정 대신 기본값으로 흘려보내 크래시 여지를
                // 남기지 않는다.
                if (items.peek(it) is SeparatedUiModel.Separator) "separator" else "episode"
            }
        ) { index ->
            when (val item = items[index] ?: return@items) {
                is SeparatedUiModel.Content -> {
                    val episode = item.data
                    EpisodeItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimension.screenPadding)
                            .animateItem(),
                        episode = episode,
                        onClick = { onSavedEpisodeClick(episode) },
                        onToggleLiked = {},
                        onToggleSaved = { onToggleSaved(episode) }
                    )
                }

                is SeparatedUiModel.Separator -> {
                    val date = item.label
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = dimension.screenPadding)
                            .padding(vertical = 8.dp),
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        pagingAppendState(
            items = items,
            key = "library:saved",
            loading = { LibraryEpisodeListSkeleton(count = 2) },
        )

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
}

@Composable
private fun FollowedContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    followedPodcastsPaging: Flow<PagingData<SeparatedUiModel<Podcast>>>,
    onFollowedPodcastClick: (Podcast) -> Unit,
    onToggleFollowed: (Podcast) -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val items = followedPodcastsPaging.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        pagingRefreshState(
            items = items,
            key = "library:followed",
            loading = { FollowedPodcastListSkeleton(count = 3) },
            empty = { LibraryEmptyMessage(text = stringResource(R.string.feature_library_followed_empty)) },
            error = { LibraryLoadErrorMessage() },
        )

        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                // peek 은 get 과 달리 페이지 로드를 부르지 않는다. enablePlaceholders=false 라
                // 이 범위에서 null 은 오지 않지만, 단정 대신 기본값으로 흘려보내 크래시 여지를
                // 남기지 않는다.
                if (items.peek(it) is SeparatedUiModel.Separator) "separator" else "podcast"
            }
        ) { index ->
            when (val item = items[index] ?: return@items) {
                is SeparatedUiModel.Content -> {
                    val podcast = item.data
                    PodcastDetailItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimension.screenPadding)
                            .animateItem(),
                        podcast = podcast,
                        onClick = { onFollowedPodcastClick(podcast) },
                        onToggleFollowed = { onToggleFollowed(podcast) }
                    )
                }

                is SeparatedUiModel.Separator -> {
                    val date = item.label
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = dimension.screenPadding)
                            .padding(vertical = 8.dp),
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }

        pagingAppendState(
            items = items,
            key = "library:followed",
            loading = { FollowedPodcastListSkeleton(count = 2) },
        )

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
}

/**
 * [RecentlyListenedContent] 목록 로딩 자리. 줄 간격·좌우 여백은 실제 목록(위 LazyColumn)과
 * 같은 값을 써야 로딩이 끝나고 실제 항목으로 바뀔 때 레이아웃이 튀지 않는다.
 */
@Composable
private fun PlayedEpisodeListSkeleton(modifier: Modifier = Modifier, count: Int) {
    val dimension = LocalDimensionTheme.current

    SkeletonContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimension.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(count) {
                PlayedEpisodeItemSkeleton(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** [LikedContent]·[SavedContent] 목록 로딩 자리. 두 탭 모두 같은 [EpisodeItem] 치수를 쓴다. */
@Composable
private fun LibraryEpisodeListSkeleton(modifier: Modifier = Modifier, count: Int) {
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

/** [FollowedContent] 목록 로딩 자리. */
@Composable
private fun FollowedPodcastListSkeleton(modifier: Modifier = Modifier, count: Int) {
    val dimension = LocalDimensionTheme.current

    SkeletonContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimension.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(count) {
                PodcastDetailItemSkeleton(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** 탭 4개가 공유하는 빈 목록 안내 문구. 탭마다 다른 [text] 를 받는다. */
@Composable
private fun LibraryEmptyMessage(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LocalDimensionTheme.current.screenPadding, vertical = 24.dp),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

/** 탭 4개가 공유하는 로드 실패 안내 문구. 실패 원인(네트워크)이 탭마다 다르지 않아 문구를 하나로 둔다. */
@Composable
private fun LibraryLoadErrorMessage(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LocalDimensionTheme.current.screenPadding, vertical = 24.dp),
        text = stringResource(R.string.feature_library_load_error),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferredContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    selectableCategories: List<SelectableCategory>,
    onCategoryClick: (Category) -> Unit = {},
    onTogglePreferred: (Category) -> Unit = {},
) {
    val dimension = LocalDimensionTheme.current

    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(dimension.screenPadding),
    ) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimension.chipSpacing),
                verticalArrangement = Arrangement.spacedBy(dimension.chipSpacing),
            ) {
                selectableCategories.forEach {
                    val category = it.category
                    EpisodiveFilterChip(
                        selected = it.isSelected,
                        onSelectedChange = { _ -> onTogglePreferred(category) },
                        // 카테고리 칩도 pill 이다 (원본 줄 144). 이것만 사각 8dp 면
                        // 바로 위 섹션 필터의 pill 칩과 모양이 어긋난다.
                        pill = true,
                        label = { Text(text = category.displayName()) },
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
}

@Composable
private fun FindBar(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFind: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // 목록을 스크롤하면 키보드를 내린다. EpisodiveSearchBar 안에도 같은 처리가 있지만
    // 그쪽은 펼침 결과 목록용이라, 이 화면의 콘텐츠 스크롤은 여기서 따로 봐야 한다.
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    EpisodiveSearchBar(
        // 좌우·아래 여백은 컴포넌트가 스스로 잡는다. 여기서 또 주면 검색 탭보다
        // 안쪽으로 밀려 같은 모양이 되지 않는다.
        modifier = modifier,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onFind,
        // 펼침은 검색 탭의 몫이다. 보관함의 이것은 상단 액션으로 여닫는 필터라,
        // 스스로 전체 화면으로 커지면 아래 목록이 사라진다.
        isExpandable = false,
        placeholder = { Text(stringResource(R.string.feature_library_find_your_library)) },
    )
}

@Composable
private fun SectionFilter(
    modifier: Modifier = Modifier,
    currentSection: LibrarySection,
    onSectionChange: (LibrarySection) -> Unit,
) {
    val sectionNames = mapOf(
        LibrarySection.All to stringResource(R.string.feature_library_filter_all),
        LibrarySection.RecentlyListened to stringResource(R.string.feature_library_filter_recently_listened),
        LibrarySection.Liked to stringResource(R.string.feature_library_filter_liked),
        LibrarySection.Saved to stringResource(R.string.feature_library_filter_saved),
        LibrarySection.Followed to stringResource(R.string.feature_library_filter_followed),
        LibrarySection.Preferred to stringResource(R.string.feature_library_filter_preferred),
    )
    val dimension = LocalDimensionTheme.current

    LazyRow(
        modifier = modifier,
        // 원본은 칩 행 아래 16px 을 띄우고 리스트가 시작한다 (원본 줄 486).
        // 이게 없으면 칩이 바로 아래 섹션 헤더에 붙는다.
        contentPadding = PaddingValues(
            start = dimension.screenPadding,
            end = dimension.screenPadding,
            bottom = 16.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(dimension.chipSpacing)
    ) {
        items(
            items = LibrarySection.entries,
            key = { it.name },
        ) { section ->
            EpisodiveFilterChip(
                selected = currentSection == section,
                onSelectedChange = { if (it) onSectionChange(section) },
                pill = true,
                label = { Text(sectionNames[section] ?: "") },
            )
        }
    }
}

@Composable
private fun FindOrFilter(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    showFind: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onFind: (String) -> Unit,
    section: LibrarySection,
    onSectionChange: (LibrarySection) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = showFind,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "search_content"
    ) { isShowingFind ->
        if (isShowingFind) {
            FindBar(
                scrollState = scrollState,
                query = query,
                onQueryChange = onQueryChange,
                onFind = onFind,
            )
        } else {
            SectionFilter(
                currentSection = section,
                onSectionChange = onSectionChange
            )
        }
    }
}

@Composable
private fun PlayedEpisodeRowSection(
    modifier: Modifier = Modifier,
    title: String,
    playedEpisodes: List<Episode>,
    onPlayedEpisodeClick: (Episode) -> Unit,
    onMore: (() -> Unit)? = null,
) {
    val dimension = LocalDimensionTheme.current

    SectionHeader(
        modifier = modifier,
        title = title,
        actionIcon = EpisodiveIcons.CaretRight.takeIf { onMore != null },
        actionIconContentDescription = onMore?.let {
            stringResource(uiR.string.core_ui_section_more_format, title)
        },
        onActionClick = onMore ?: {},
    ) {
        val lazyListState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(
            lazyListState = lazyListState,
            snapPosition = SnapPosition.Start,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
            flingBehavior = flingBehavior,
            horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
        ) {
            items(
                count = playedEpisodes.size,
                key = { playedEpisodes[it].id },
            ) {
                PlayedEpisodeItem(
                    modifier = Modifier.width(250.dp),
                    playedEpisode = playedEpisodes[it],
                    onClick = { onPlayedEpisodeClick(playedEpisodes[it]) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeRowSection(
    modifier: Modifier = Modifier,
    title: String,
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    onMore: (() -> Unit)? = null,
) {
    val dimension = LocalDimensionTheme.current

    SectionHeader(
        modifier = modifier,
        title = title,
        actionIcon = EpisodiveIcons.CaretRight.takeIf { onMore != null },
        actionIconContentDescription = onMore?.let {
            stringResource(uiR.string.core_ui_section_more_format, title)
        },
        onActionClick = onMore ?: {},
    ) {
        val lazyListState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(
            lazyListState = lazyListState,
            snapPosition = SnapPosition.Start,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
            flingBehavior = flingBehavior,
            horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
        ) {
            items(
                count = episodes.size,
                key = { episodes[it].id },
            ) {
                EpisodeDetailItem(
                    episode = episodes[it],
                    onClick = { onEpisodeClick(episodes[it]) }
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    modifier: Modifier = Modifier,
    title: String,
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    onMore: (() -> Unit)? = null,
) {
    val dimension = LocalDimensionTheme.current

    SectionHeader(
        modifier = modifier,
        title = title,
        actionIcon = EpisodiveIcons.CaretRight.takeIf { onMore != null },
        actionIconContentDescription = onMore?.let {
            stringResource(uiR.string.core_ui_section_more_format, title)
        },
        onActionClick = onMore ?: {},
    ) {
        val lazyListState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(
            lazyListState = lazyListState,
            snapPosition = SnapPosition.Start,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
            flingBehavior = flingBehavior,
            horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
        ) {
            items(
                items = categories,
                key = { it.id },
            ) { category ->
                CategoryItem(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun LibraryScreenPreview() {
    EpisodiveTheme {
        LibraryScreen(
            query = "test",
            onQueryChange = {},
            onFind = {},
            section = LibrarySection.All,
            playedEpisodes = episodeTestDataList,
            likedEpisodes = episodeTestDataList,
            savedEpisodes = episodeTestDataList,
            followedPodcasts = podcastTestDataList,
            preferredCategories = Category.entries,
            playedEpisodesPaging = flowOf(PagingData.from(episodeTestDataList.map {
                SeparatedUiModel.Content(it)
            })),
            likedEpisodesPaging = flowOf(PagingData.from(episodeTestDataList.map {
                SeparatedUiModel.Content(it)
            })),
            savedEpisodesPaging = flowOf(PagingData.from(episodeTestDataList.map {
                SeparatedUiModel.Content(it)
            })),
            followedPodcastsPaging = flowOf(PagingData.from(podcastTestDataList.map {
                SeparatedUiModel.Content(it)
            })),
            selectableCategories = Category.entries.map { category ->
                SelectableCategory(
                    category = category,
                    isSelected = true,
                )
            },
        )
    }
}

@DevicePreviews
@Composable
private fun LibrarySkeletonPreview() {
    EpisodiveTheme {
        LibrarySkeleton()
    }
}