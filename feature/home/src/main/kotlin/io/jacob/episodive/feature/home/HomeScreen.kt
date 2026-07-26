package io.jacob.episodive.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodiveDragHandle
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.screen.LoadingScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveHeroGradientEnd
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.EpisodiveHeroGradientStart
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.mapper.toHumanReadable
import io.jacob.episodive.core.testing.model.channelTestDataList
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.liveEpisodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.ui.ChannelSection
import io.jacob.episodive.core.ui.R as uiR
import io.jacob.episodive.core.ui.EpisodesSection
import io.jacob.episodive.core.ui.PodcastsSection


@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onPodcastClick: (Long) -> Unit,
    onChannelClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val unsavedMessage = stringResource(uiR.string.core_ui_snackbar_unsaved)
    val undoLabel = stringResource(uiR.string.core_ui_snackbar_undo)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToPodcast -> onPodcastClick(effect.podcastId)
                is HomeEffect.NavigateToChannel -> onChannelClick(effect.channelId)
                is HomeEffect.ShowUnsaveSnackbar -> {
                    val undone = onShowSnackbar(unsavedMessage, undoLabel)
                    if (undone) viewModel.sendAction(HomeAction.ToggleSavedEpisode(effect.episode))
                }
            }
        }
    }

    when (val s = state) {
        is HomeState.Loading -> LoadingScreen()

        is HomeState.Success -> HomeScreen(
            modifier = modifier
                .fillMaxSize(),
            playingEpisodes = s.playingEpisodes,
            userRecentPodcasts = s.userRecentPodcasts,
            randomEpisodes = s.randomEpisodes,
            userTrendingPodcasts = s.userTrendingPodcasts,
            followedPodcasts = s.followedPodcasts,
            localTrendingPodcasts = s.localTrendingPodcasts,
            foreignTrendingPodcasts = s.foreignTrendingPodcasts,
            liveEpisodes = s.liveEpisodes,
            channels = s.channels,
            onPlayEpisode = { viewModel.sendAction(HomeAction.PlayEpisode(it)) },
            onResumeEpisode = { viewModel.sendAction(HomeAction.ResumeEpisode(it)) },
            onToggleLikedEpisode = { viewModel.sendAction(HomeAction.ToggleLikedEpisode(it)) },
            onToggleSavedEpisode = { viewModel.sendAction(HomeAction.ToggleSavedEpisode(it)) },
            onPodcastClick = { viewModel.sendAction(HomeAction.ClickPodcast(it)) },
            onChannelClick = { viewModel.sendAction(HomeAction.ClickChannel(it)) },
        )

        is HomeState.Error -> ErrorScreen(message = s.message)
    }
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    playingEpisodes: List<Episode>,
    userRecentPodcasts: List<Podcast>,
    randomEpisodes: List<Episode>,
    userTrendingPodcasts: List<Podcast>,
    followedPodcasts: List<Podcast>,
    localTrendingPodcasts: List<Podcast>,
    foreignTrendingPodcasts: List<Podcast>,
    liveEpisodes: List<Episode>,
    channels: List<Channel>,
    onPlayEpisode: (Episode) -> Unit,
    onResumeEpisode: (Episode) -> Unit,
    onToggleLikedEpisode: (Episode) -> Unit,
    onToggleSavedEpisode: (Episode) -> Unit = {},
    onPodcastClick: (Long) -> Unit,
    onChannelClick: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = this.maxHeight

        val density = LocalDensity.current
        var topBarHeight by remember { mutableStateOf(80.dp) }
        var contentHeight by remember { mutableStateOf(0.dp) }

        val sheetExpandHeight = screenHeight - topBarHeight - 32.dp
        // 이어듣기 캐러셀 높이만큼만 시트를 내려 둔다. 시트를 위로 끌면 그 위를 덮어
        // 목록에 화면 전체를 쓸 수 있다.
        val sheetPartiallyExpandHeight = sheetExpandHeight - contentHeight

        val sheetState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
                skipHiddenState = true
            )
        )

        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = sheetState,
            topBar = {
                Column(
                    modifier = Modifier
                        .onSizeChanged { size ->
                            topBarHeight = with(density) { size.height.toDp() }
                        },
                ) {
                    HomeHeader(
                        title = stringResource(R.string.feature_home_title),
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .onSizeChanged { size ->
                            contentHeight = with(density) {
                                size.height.toDp().coerceIn(0.dp, HomeHeroMaxPeekHeight)
                            }
                        },
                ) {
                    if (playingEpisodes.isNotEmpty()) {
                        HomeContinueListeningRow(
                            episodes = playingEpisodes,
                            onEpisodeClick = onResumeEpisode,
                        )

                        Spacer(modifier = Modifier.height(HomeHeroSheetGap))
                    }
                }
            },
            sheetPeekHeight = sheetPartiallyExpandHeight,
            sheetDragHandle = { EpisodiveDragHandle() },
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = sheetExpandHeight)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        // 콘텐츠가 짧아 fling 이 대부분 가장자리에서 끝나는데, 이때 잔여
                        // 속도·드래그가 stretch 로 흡수되며 스크롤이 멈추는 순간 콘텐츠가
                        // 반대 방향으로 움찔거리므로 overscroll 을 사용하지 않는다.
                        // 상단 가장자리는 시트 드래그(collapse)가 피드백을 대신한다.
                        overscrollEffect = null,
                    ) {
                        itemWithDivider {
                            PodcastsSection(
                                title = stringResource(R.string.feature_home_section_my_recent_feeds),
                                podcasts = userRecentPodcasts,
                                subtitleProvider = { it.ownerName.ifEmpty { it.author } },
                                onPodcastClick = { feed ->
                                    onPodcastClick(feed.id)
                                }
                            )
                        }

                        itemWithDivider {
                            EpisodesSection(
                                title = stringResource(R.string.feature_home_section_random_episodes),
                                episodes = randomEpisodes,
                                onEpisodeClick = onPlayEpisode,
                                onToggleLikedEpisode = onToggleLikedEpisode,
                                onToggleSavedEpisode = onToggleSavedEpisode,
                            )
                        }

                        itemWithDivider {
                            PodcastsSection(
                                title = stringResource(R.string.feature_home_section_my_trending_feeds),
                                podcasts = userTrendingPodcasts,
                                subtitleProvider = { it.ownerName.ifEmpty { it.author } },
                                onPodcastClick = { feed ->
                                    onPodcastClick(feed.id)
                                }
                            )
                        }

                        itemWithDivider {
                            PodcastsSection(
                                title = stringResource(R.string.feature_home_section_followed_podcasts),
                                podcasts = followedPodcasts,
                                onMore = {

                                },
                                onPodcastClick = { podcast ->
                                    onPodcastClick(podcast.id)
                                }
                            )
                        }

                        itemWithDivider {
                            PodcastsSection(
                                title = stringResource(R.string.feature_home_section_trending_in_local),
                                podcasts = localTrendingPodcasts,
                                subtitleProvider = { it.ownerName.ifEmpty { it.author } },
                                onPodcastClick = { feed ->
                                    onPodcastClick(feed.id)
                                }
                            )
                        }

                        itemWithDivider {
                            PodcastsSection(
                                title = stringResource(R.string.feature_home_section_trending_in_foreign),
                                podcasts = foreignTrendingPodcasts,
                                subtitleProvider = { it.ownerName.ifEmpty { it.author } },
                                onPodcastClick = { feed ->
                                    onPodcastClick(feed.id)
                                }
                            )
                        }

                        itemWithDivider {
                            EpisodesSection(
                                title = stringResource(R.string.feature_home_section_live_episodes),
                                episodes = liveEpisodes,
                                onEpisodeClick = onPlayEpisode,
                                onToggleLikedEpisode = onToggleLikedEpisode,
                                onToggleSavedEpisode = onToggleSavedEpisode,
                            )
                        }

                        item {
                            ChannelSection(
                                title = stringResource(R.string.feature_home_section_channels),
                                channels = channels,
                                onChannelClick = onChannelClick
                            )
                        }

                        item {
                            // 미니플레이어 높이 + 마진만큼 비워야 마지막 항목이 가리지 않는다.
                            val dimension = LocalDimensionTheme.current
                            Spacer(
                                modifier = Modifier.height(
                                    dimension.playerBarSpace,
                                )
                            )
                        }
                    }
                }
            },
        )
    }
}

fun LazyListScope.itemWithDivider(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    item(key, contentType, content)
    item {
        HorizontalDivider(modifier = Modifier.padding(16.dp))
    }
}

/** 이어듣기 캐러셀이 바텀시트 밖으로 내밀 수 있는 최대 높이. */
private val HomeHeroMaxPeekHeight = 240.dp

/**
 * 이어듣기 캐러셀과 바텀시트 사이 간격. 시트는 자기 드래그 핸들(위아래 여백 포함 34dp)을
 * 이미 이고 있으므로 여기서 더 벌리면 둘 사이가 크게 비어 보인다.
 */
private val HomeHeroSheetGap = 6.dp

/**
 * 이어듣기 카드가 차지하는 화면 폭 비율. 1 보다 작게 두어 다음 카드가 오른쪽에 살짝
 * 걸치게 하고, 그것으로 옆으로 넘길 수 있다는 신호를 준다.
 */
private const val HomeHeroWidthFraction = 0.78f

/** 커버 팔레트가 준비되기 전 쓰는 이어듣기 카드 그라디언트 양 끝. */
private val HomeHeroFallbackStart = EpisodiveHeroGradientStart
private val HomeHeroFallbackEnd = EpisodiveHeroGradientEnd

/**
 * 추출색으로 카드 그라디언트 양 끝을 만들 때 검정 쪽으로 섞는 비율.
 * 시작 쪽도 조금 눌러 둔다 — 밝은 커버에서 뽑힌 색 위에서는 흰 제목이 묻힌다.
 */
private const val HomeHeroGradientStartBlend = 0.2f
private const val HomeHeroGradientEndBlend = 0.6f

/** 이어듣기 카드 안쪽 여백과 커버 크기. */
private val HomeHeroPadding = 14.dp
private const val HomeHeroCoverSizeDp = 60
private val HomeHeroCoverSize = HomeHeroCoverSizeDp.dp

@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    title: String,
) {
    val dimension = LocalDimensionTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            // 홈은 BottomSheetScaffold의 topBar 슬롯을 쓰지 않아 인셋이 자동으로 오지 않는다.
            // 이게 없으면 인사말이 상태바 시계와 겹친다.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                start = dimension.headerPadding,
                end = dimension.headerPadding,
                top = 16.dp,
                bottom = 8.dp,
            ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 이어듣기 캐러셀. 카드 한 장이 화면을 다 차지하지 않게 해 다음 카드가 살짝 보이고,
 * 스냅 스크롤로 한 장씩 넘어간다.
 */
@Composable
private fun HomeContinueListeningRow(
    modifier: Modifier = Modifier,
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val lazyListState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = lazyListState,
        snapPosition = SnapPosition.Start,
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(horizontal = dimension.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
        // 짧은 캐러셀은 가장자리 stretch 가 릴리즈 시 움찔거림으로 남는다.
        overscrollEffect = null,
    ) {
        items(
            items = episodes,
            key = { it.id },
        ) { episode ->
            HomeContinueListeningHero(
                modifier = Modifier.fillParentMaxWidth(HomeHeroWidthFraction),
                episode = episode,
                onClick = { onEpisodeClick(episode) },
            )
        }
    }
}

@Composable
private fun HomeContinueListeningHero(
    modifier: Modifier = Modifier,
    episode: Episode,
    onClick: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current
    val remain = episode.remain
    val leftLabel = stringResource(uiR.string.core_ui_left)

    // 카드 배경은 이 에피소드 커버에서 뽑은 색으로 흐른다. 팔레트가 준비되기 전에는
    // 기존 브랜드 그라디언트를 그대로 쓴다.
    var dominantColor by remember(episode.id) { mutableStateOf<Color?>(null) }
    val heroGradient = remember(dominantColor) {
        val start = dominantColor
            ?.let { lerp(it, Color.Black, HomeHeroGradientStartBlend) }
            ?: HomeHeroFallbackStart
        val end = dominantColor
            ?.let { lerp(it, Color.Black, HomeHeroGradientEndBlend) }
            ?: HomeHeroFallbackEnd
        Brush.linearGradient(listOf(start, end))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(heroGradient)
            .clickable(onClick = onClick)
            .padding(HomeHeroPadding),
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StateImage(
                    modifier = Modifier
                        .size(HomeHeroCoverSize)
                        .clip(EpisodiveShapes.coverForSize(HomeHeroCoverSizeDp)),
                    imageUrl = episode.image.ifEmpty { episode.feedImage },
                    contentDescription = episode.title,
                    // 카드 그라디언트를 이 커버 색으로 물들이기 위해 위로 올린다.
                    onDominantColorExtracted = { dominantColor = it },
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(uiR.string.core_ui_continue),
                        // 원본은 11/700/.05em (원본 줄 182). labelMedium(13)은 두 단계 크다.
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.05.em),
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 남은 시간은 진행바 아래가 아니라 재생 버튼이 있던 오른쪽 끝에 둔다.
            // 한 줄로 합치면서 카드 높이도 그만큼 줄어든다.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .height(dimension.progressThickness)
                        .clip(CircleShape),
                    progress = { episode.progress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.16f),
                    // M3 기본 gapSize 4dp 를 끄지 않으면 채움과 트랙 사이가 끊겨 보인다.
                    // 원본은 끊김 없는 한 줄이다 (원본 줄 185).
                    gapSize = (-4).dp,
                    drawStopIndicator = {},
                )

                Text(
                    // 목록 행과 같은 표기를 쓴다. 분 단위로 직접 만들면 로케일이 안 붙고
                    // 1분 미만이 "0분 남음"이 된다.
                    text = remain?.let { "${it.toHumanReadable()} $leftLabel" }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun HomeScreenPreview() {
    EpisodiveTheme {
        HomeScreen(
            playingEpisodes = episodeTestDataList,
            userRecentPodcasts = podcastTestDataList,
            randomEpisodes = episodeTestDataList,
            userTrendingPodcasts = podcastTestDataList,
            followedPodcasts = podcastTestDataList,
            localTrendingPodcasts = podcastTestDataList,
            foreignTrendingPodcasts = podcastTestDataList,
            liveEpisodes = liveEpisodeTestDataList,
            channels = channelTestDataList,
            onPlayEpisode = {},
            onResumeEpisode = {},
            onToggleLikedEpisode = {},
            onPodcastClick = {},
            onChannelClick = {},
        )
    }
}