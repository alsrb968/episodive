package io.jacob.episodive.feature.clip

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.jacob.episodive.core.designsystem.component.SkeletonContainer
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Playback
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.ui.EpisodeClipItem
import io.jacob.episodive.core.ui.EpisodeClipItemSkeleton
import io.jacob.episodive.core.ui.PagingRefreshPhase
import io.jacob.episodive.core.ui.refreshPhase
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

    // itemCount 만 보고 로딩을 그리면 결과가 0건이거나 로드가 실패했을 때 스피너가 영원히
    // 돈다(OnboardingScreen.kt 의 PodcastSelectionScreen 이 겪은 것과 같은 함정). refreshPhase()
    // 로 로딩/빈 목록/오류를 갈라 각각 다른 화면을 보여준다.
    when (episodesPaging.refreshPhase()) {
        PagingRefreshPhase.Loading -> {
            ClipSkeleton(modifier)
            return
        }

        PagingRefreshPhase.Empty -> {
            ClipMessage(modifier, stringResource(R.string.feature_clip_empty))
            return
        }

        PagingRefreshPhase.Error -> {
            ClipMessage(modifier, stringResource(R.string.feature_clip_error))
            return
        }

        PagingRefreshPhase.Content -> Unit
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
                // 이 카드가 지금 플레이어에 올라 있는 그 클립인지. 파도 애니메이션과 남은
                // 시간은 한 배지의 두 짝이므로 반드시 같은 기준으로 갈라야 한다. 애니메이션만
                // currentPage 로 가르면, 그 값은 스와이프 50% 지점에서 뒤집히는 반면
                // progress 는 페이지가 멎은 뒤에야 따라와서 — 넘기는 내내 들어오는 카드가
                // "재생 중 파도 + 멈춘 시간"을, 나가는 카드가 "멈춘 파도 + 흐르는 시간"을
                // 보여준다.
                val isCurrentClip = progress.episodeId == episode.id

                EpisodeClipItem(
                    modifier = Modifier.fillMaxSize(),
                    episode = episode,
                    isPlaying = isPlaying && isCurrentClip,
                    // 흐르는 남은 시간은 지금 플레이어에 올라 있는 클립의 것뿐이다. 아직
                    // 자기 차례가 아닌 카드(첫 진입, 스와이프 직후, 위아래로 걸쳐 보이는
                    // 이웃)까지 같은 값을 쓰면 남의 진행 시간을 빌려 보여주게 되고, 재생이
                    // 시작되는 순간 숫자가 튄다. 그런 카드는 자기 클립의 전체 길이를 보여준다.
                    remaining = if (isCurrentClip) {
                        progress.remaining
                    } else {
                        episode.clipPlaybackDuration
                    },
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
 * 첫 화면 로딩 자리. 페이저가 아직 없으니 카드 한 장만 보여준다.
 *
 * 제목은 스켈레톤으로 흉내내지 않고 [ClipTitle] 을 그대로 렌더한다 — 정적 문자열이라 실제
 * 화면으로 넘어갈 때 흔들리지 않아야 하기 때문이다. progress 는 1f 로 고정해 완전히 보이는
 * 상태로 둔다(스와이프가 없으니 밀려 올라갈 일도 없다).
 */
@Composable
private fun ClipSkeleton(modifier: Modifier = Modifier) {
    val titleProgress = remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // SkeletonContainer 바깥에서 칠한다 — 안에서 칠하면 shimmerSweep 의 오프스크린
            // 레이어 전체가 "그려진 픽셀"이 되어 화면이 통째로 균일하게 쓸린다.
            .background(ClipBackgroundFallback),
    ) {
        SkeletonContainer(
            // 실제 VerticalPager의 contentPadding(L263-269)과 동일하게 맞춘다.
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = ClipPageHorizontalPadding,
                    end = ClipPageHorizontalPadding,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                            ClipTitleHeight,
                    bottom = LocalDimensionTheme.current.playerBarSpace + ClipPageBottomGap,
                ),
        ) {
            EpisodeClipItemSkeleton(modifier = Modifier.fillMaxSize())
        }

        ClipTitle(
            modifier = Modifier.align(Alignment.TopStart),
            title = stringResource(R.string.feature_clip_title),
            progress = titleProgress,
        )
    }
}

/** 클립 결과가 없거나 로드에 실패했을 때의 안내 문구. */
@Composable
private fun ClipMessage(modifier: Modifier = Modifier, message: String) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClipBackgroundFallback),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
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