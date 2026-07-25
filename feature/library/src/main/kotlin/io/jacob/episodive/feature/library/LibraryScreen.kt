package io.jacob.episodive.feature.library

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.jacob.episodive.core.designsystem.component.EpisodiveFilterChip
import io.jacob.episodive.core.designsystem.component.EpisodiveScaffold
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.screen.LoadingScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.ui.R as uiR
import io.jacob.episodive.core.ui.CategoryItem
import io.jacob.episodive.core.ui.EpisodeDetailItem
import io.jacob.episodive.core.ui.EpisodeItem
import io.jacob.episodive.core.ui.PlayedEpisodeItem
import io.jacob.episodive.core.ui.PodcastDetailItem
import io.jacob.episodive.core.ui.PodcastsSection
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
        is LibraryState.Loading -> LoadingScreen()

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

        is LibraryState.Error -> ErrorScreen(message = s.message)
    }
}

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

    EpisodiveScaffold(
        modifier = modifier,
        title = stringResource(R.string.feature_library_title),
        subTitle = {
            FindOrFilter(
                scrollState = scrollState,
                showFind = showFind,
                onShowFindChanged = { showFind = it },
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
                onPodcastClick = onPodcastClick
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
                    )
                }

                if (likedEpisodes.isNotEmpty()) {
                    EpisodeRowSection(
                        title = stringResource(R.string.feature_library_section_liked_episodes),
                        episodes = likedEpisodes,
                        onEpisodeClick = onEpisodeClick,
                    )
                }

                if (savedEpisodes.isNotEmpty()) {
                    EpisodeRowSection(
                        title = stringResource(R.string.feature_library_section_saved_episodes),
                        episodes = savedEpisodes,
                        onEpisodeClick = onEpisodeClick,
                    )
                }

                if (followedPodcasts.isNotEmpty()) {
                    PodcastsSection(
                        title = stringResource(R.string.feature_library_section_followed_podcasts),
                        podcasts = followedPodcasts,
                        onPodcastClick = onPodcastClick,
                    )
                }

                if (preferredCategories.isNotEmpty()) {
                    CategorySection(
                        title = stringResource(R.string.feature_library_section_preferred_categories),
                        categories = preferredCategories,
                        onCategoryClick = {},
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
        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                when (items[it]) {
                    is SeparatedUiModel.Content -> "episode"
                    is SeparatedUiModel.Separator -> "separator"
                    null -> "loading"
                }
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
        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                when (items[it]) {
                    is SeparatedUiModel.Content -> "episode"
                    is SeparatedUiModel.Separator -> "separator"
                    null -> "loading"
                }
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
        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                when (items[it]) {
                    is SeparatedUiModel.Content -> "episode"
                    is SeparatedUiModel.Separator -> "separator"
                    null -> "loading"
                }
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
        items(
            count = items.itemCount,
            key = items.itemKey {
                when (it) {
                    is SeparatedUiModel.Content -> it.data.id
                    is SeparatedUiModel.Separator -> it.label
                }
            },
            contentType = {
                when (items[it]) {
                    is SeparatedUiModel.Content -> "podcast"
                    is SeparatedUiModel.Separator -> "separator"
                    null -> "loading"
                }
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

        item {
            Spacer(modifier = Modifier.height(LocalDimensionTheme.current.playerBarSpace))
        }
    }
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
                        label = { Text(text = category.label) },
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
    onDismiss: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    SearchBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimension.screenPadding)
            .padding(bottom = 16.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    onFind(query)
                    keyboardController?.hide()
                },
                expanded = false,
                onExpandedChange = { if (!it) onDismiss() },
                placeholder = { Text(stringResource(R.string.feature_library_find_your_library)) },
                leadingIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(EpisodiveIcons.Search, null)
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = { if (!it) onDismiss() },
        content = {}
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
    onShowFindChanged: (Boolean) -> Unit,
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
                onDismiss = { onShowFindChanged(false) },
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
) {
    val dimension = LocalDimensionTheme.current

    SectionHeader(
        modifier = modifier,
        title = title,
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
) {
    val dimension = LocalDimensionTheme.current

    SectionHeader(
        modifier = modifier,
        title = title,
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
) {
    val dimension = LocalDimensionTheme.current

    SectionHeader(
        modifier = modifier,
        title = title,
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