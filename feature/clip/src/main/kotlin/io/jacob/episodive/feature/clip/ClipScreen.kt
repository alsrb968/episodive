package io.jacob.episodive.feature.clip

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.jacob.episodive.core.designsystem.screen.LoadingScreen
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Playback
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.ui.EpisodeClipItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
internal fun ClipRoute(
    modifier: Modifier = Modifier,
    viewModel: ClipViewModel = hiltViewModel(),
    onPodcastClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.sendAction(ClipAction.Resume)
                Lifecycle.Event.ON_STOP -> viewModel.sendAction(ClipAction.Pause)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ClipEffect.NavigateToPodcast -> onPodcastClick(effect.podcastId)
            }
        }
    }

    val clipPlayerState by viewModel.clipPlayerState.collectAsStateWithLifecycle()

    ClipScreen(
        modifier = modifier,
        episodes = viewModel.episodes,
        playback = clipPlayerState.playback,
        progress = clipPlayerState.progress,
        isPlaying = clipPlayerState.isPlaying,
        onEpisodeChanged = { viewModel.sendAction(ClipAction.PlayClip(it)) },
        onEpisodeClick = { viewModel.sendAction(ClipAction.ClickEpisode(it)) },
        onToggleLikedEpisode = { viewModel.sendAction(ClipAction.ToggleLikedEpisode(it)) },
        onPodcastClick = { viewModel.sendAction(ClipAction.ClickPodcast(it)) },
        onShowSnackbar = onShowSnackbar,
    )
}

@Composable
internal fun ClipScreen(
    modifier: Modifier = Modifier,
    episodes: Flow<PagingData<Episode>>,
    playback: Playback,
    progress: Progress,
    isPlaying: Boolean,
    onEpisodeChanged: (Episode) -> Unit = {},
    onEpisodeClick: (Episode) -> Unit = {},
    onToggleLikedEpisode: (Episode) -> Unit = {},
    onPodcastClick: (Long) -> Unit = {},
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean = { _, _ -> false },
) {
    val backgroundColor = MaterialTheme.colorScheme.background

    // 배경은 지금 보고 있는 클립의 커버 색을 따라간다. 팔레트가 아직 없으면 기본색.
    var dominantColor by remember { mutableStateOf(ClipBackgroundFallback) }
    // 페이지를 넘길 때 배경이 툭 바뀌지 않도록 색만 부드럽게 잇는다.
    val gradientColor by animateColorAsState(dominantColor, label = "clipBackground")
    val midColor = lerp(gradientColor, backgroundColor, 0.55f)
    val edgeColor = lerp(gradientColor, backgroundColor, 0.88f)

    Box(
        modifier = modifier
            .fillMaxSize()
            // 배경: 폭 50% / 높이 38% 지점을 중심으로 한 radial 그라디언트 + 검정 30% 오버레이 (원본 줄 455).
            .drawWithCache {
                val brush = Brush.radialGradient(
                    0f to gradientColor,
                    0.62f to midColor,
                    1f to edgeColor,
                    center = Offset(size.width * 0.5f, size.height * 0.38f),
                    radius = size.width * 1.2f,
                )
                onDrawBehind {
                    drawRect(brush)
                    drawRect(ClipBackgroundOverlayColor)
                }
            },
    ) {
        EpisodeClipPager(
            modifier = Modifier.fillMaxSize(),
            episodes = episodes,
            playback = playback,
            progress = progress,
            isPlaying = isPlaying,
            onEpisodeChanged = onEpisodeChanged,
            onEpisodeClick = onEpisodeClick,
            onToggleLikedEpisode = onToggleLikedEpisode,
            onPodcastClick = onPodcastClick,
            onCurrentDominantColor = { dominantColor = it },
        )
    }
}

/** 커버 팔레트가 준비되기 전 쓰는 클립 배경색 (원본 줄 455). */
private val ClipBackgroundFallback = Color(0xFF2E5F47)
private val ClipBackgroundOverlayColor = Color.Black.copy(alpha = 0.18f)

/** 화면 제목이 차지하는 높이·여백 — 다른 탭의 상단 바(64dp, 좌측 16dp)에 맞춘다. */
private val ClipTitleHeight = 56.dp
private val ClipTitleHorizontalPadding = 16.dp

/** 클립 카드 여백. 하단은 떠 있는 미니플레이어에 닿지 않을 만큼만 띄운다. */
private val ClipPageHorizontalPadding = 16.dp
private val ClipPageBottomGap = 8.dp
private val ClipPageSpacing = 24.dp

@Composable
fun EpisodeClipPager(
    modifier: Modifier = Modifier,
    episodes: Flow<PagingData<Episode>>,
    playback: Playback,
    progress: Progress,
    isPlaying: Boolean,
    onEpisodeChanged: (Episode) -> Unit = {},
    onEpisodeClick: (Episode) -> Unit = {},
    onToggleLikedEpisode: (Episode) -> Unit = {},
    onPodcastClick: (Long) -> Unit = {},
    onCurrentDominantColor: (Color) -> Unit = {},
) {
    val episodesPaging = episodes.collectAsLazyPagingItems()

    if (episodesPaging.itemCount == 0) {
        LoadingScreen()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { episodesPaging.itemCount }
    )

    // 팔레트 색은 이미지가 "로드될 때" 한 번만 올라온다. 다음 페이지는 미리보기로 이미
    // 로드된 뒤라 넘어가는 시점에는 새 이벤트가 없다. 그래서 페이지별로 색을 모아 두고,
    // 현재 페이지가 바뀔 때 그 색을 골라 배경에 넘긴다.
    val pageColors = remember { mutableStateMapOf<Int, Color>() }
    val currentPageColor = pageColors[pagerState.currentPage]

    LaunchedEffect(currentPageColor) {
        currentPageColor?.let(onCurrentDominantColor)
    }

    // 첫 번째 에피소드 자동 재생 (최초 한 번만)
    LaunchedEffect(Unit) {
        snapshotFlow { episodesPaging.itemCount }
            .filter { it > 0 }
            .take(1)
            .collectLatest {
                episodesPaging[0]?.let { firstEpisode ->
                    onEpisodeChanged(firstEpisode)
                }
            }
    }

    // 페이지가 변경되면 해당 에피소드 재생
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                episodesPaging[page]?.let { episode ->
                    onEpisodeChanged(episode)
                }
            }
    }

    // 재생 완료 시 다음 페이지로 이동
    LaunchedEffect(playback) {
        if (playback == Playback.ENDED) {
            val nextPage = pagerState.currentPage + 1
            if (nextPage < episodesPaging.itemCount) {
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // 첫 클립에서 다음으로 넘어가는 정도(1 = 첫 클립, 0 = 넘어간 뒤). 제목이 이만큼만 보인다.
    val titleProgress = remember {
        derivedStateOf {
            (1f - (pagerState.currentPage + pagerState.currentPageOffsetFraction))
                .coerceIn(0f, 1f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { episodesPaging.peek(it)?.id ?: it },
            pageSpacing = ClipPageSpacing, // 이전/다음 컨텐츠가 보이는 간격
            // 상태바 몫을 여기(contentPadding)에 넣는다. 페이저 바깥에 패딩으로 주면 페이저
            // 자체가 상태바 아래에서 시작해, 위로 걸쳐 보여야 할 이전 클립이 그 선에서 잘린다.
            contentPadding = PaddingValues(
                start = ClipPageHorizontalPadding,
                end = ClipPageHorizontalPadding,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                        ClipTitleHeight,
                bottom = LocalDimensionTheme.current.playerBarSpace + ClipPageBottomGap,
            )
        ) { page ->
            episodesPaging[page]?.let { episode ->
                EpisodeClipItem(
                    modifier = Modifier.fillMaxSize(),
                    episode = episode,
                    isPlaying = isPlaying && page == pagerState.currentPage,
                    remaining = progress.remaining,
                    onClick = {
                        onEpisodeClick(episode)
                    },
                    onPlayEpisode = {
                        onEpisodeChanged(episode)
                    },
                    onToggleLikedEpisode = {
                        onToggleLikedEpisode(episode)
                    },
                    onDominantColorExtracted = { color -> pageColors[page] = color },
                )
            }
        }

        ClipTitle(
            modifier = Modifier.align(Alignment.TopStart),
            title = stringResource(R.string.feature_clip_title),
            progress = titleProgress,
        )
    }
}

/**
 * 다른 탭과 같은 화면 제목. 다음 클립으로 넘어가는 만큼 위로 밀려 올라가며 사라진다.
 *
 * 진행률은 [State] 그대로 받아 graphicsLayer 안에서 읽는다. 컴포지션에서 풀면 스와이프
 * 프레임마다 재구성된다.
 */
@Composable
private fun ClipTitle(
    modifier: Modifier = Modifier,
    title: String,
    progress: State<Float>,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val visible = progress.value
                alpha = visible
                translationY = -(1f - visible) * size.height
            }
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(ClipTitleHeight)
            .padding(horizontal = ClipTitleHorizontalPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@DevicePreviews
@Composable
private fun ClipScreenPreview() {
    EpisodiveTheme {
        ClipScreen(
            episodes = flowOf(
                PagingData.from(
                    episodeTestDataList.map {
                        it.copy(
                            clipStartTime = Instant.fromEpochMilliseconds(60_000L),
                            clipDuration = 1278.seconds,
                        )
                    }
                )),
            playback = Playback.READY,
            progress = Progress(
                position = 1000L.seconds,
                buffered = 1278.seconds,
                duration = 2000L.seconds,
            ),
            isPlaying = true,
        )
    }
}