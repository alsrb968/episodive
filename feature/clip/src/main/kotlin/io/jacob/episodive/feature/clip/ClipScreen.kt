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
import androidx.compose.runtime.rememberUpdatedState
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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
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

    // 이펙트는 한 번 뜨면 다시 돌지 않으므로, 그 안에서 읽는 것은 붙들지 말고 최신을 본다.
    // episodesPaging 도 마찬가지다 — episodes 흐름이 바뀌면 새 LazyPagingItems 가 서는데,
    // 그때 pagerState 는 살아남아 이펙트가 다시 뜨지 않으므로 죽은 인스턴스를 들여다보게 된다.
    val currentEpisodes by rememberUpdatedState(episodesPaging)
    val currentOnEpisodeChanged by rememberUpdatedState(onEpisodeChanged)

    // 페이지가 멎으면 그 클립을 재생한다. 첫 진입도 이 한 곳이 맡는다.
    //
    // 예전에는 "첫 클립 자동 재생" 이펙트를 따로 두었는데, settledPage 가 처음부터 0 을
    // 내보내는 데다 이 지점은 이미 Content(= itemCount > 0)라서 둘이 반드시 겹쳤다.
    //
    // **무엇을 재생할지는 페이지 번호로 정한다.** 그 자리의 에피소드를 직접 흘려보내면,
    // Paging 이 목록을 다시 불러올 때 같은 자리에 다른 에피소드가 앉아 듣고 있던 클립이
    // 갈아치워진다. 가정이 아니라 실제 경로다 — 좋아요를 누르면 SoundbiteEpisodePagingSource
    // 가 liked_episodes 무효화로 refresh 하고, getRefreshKey 가 앵커 기준으로 창을 옮긴다.
    //
    // 번호만 보면 아직 로드되지 않은 자리에서 아무 일도 못 하고 끝나므로, 그 자리의 항목이
    // 도착할 때까지 기다렸다가 재생한다. 기다리는 동안 페이지가 또 바뀌면 collectLatest 가
    // 그 대기를 걷어낸다 — 여기서는 안쪽이 실제로 suspend 라 끊을 것이 있다.
    //
    // **같은 클립을 두 번 올리지 않게 막는 일은 여기서 하지 않는다.** 화면이 "무엇을 올려
    // 뒀는지" 를 기억하려 하면 그 기억이 컴포지션과 함께 사라지거나(탭 전환) 실제와 어긋난다
    // (요청과 progress 사이의 지연). 실제로 무엇이 올라가 있는지는 플레이어가 알고 있으므로,
    // 판단을 PlayerDataSource.playClip 에 맡기고 여기서는 요청만 보낸다.
    //
    // 읽기는 `episodesPaging[i]` 가 아니라 itemSnapshotList 로 한다. 전자는 범위를 벗어나면
    // 예외를 던지고, 조회 자체가 Paging 에 "이 언저리를 미리 불러라" 는 힌트를 남기는
    // 부작용이라 페이저가 실제로 그리는 페이지의 힌트와 다툰다.
    // snapshotFlow 는 이미 연속된 같은 값을 삼키므로 distinctUntilChanged 를 덧붙이지 않는다.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collectLatest { page ->
                val episode = snapshotFlow { currentEpisodes.itemSnapshotList.getOrNull(page) }
                    .filterNotNull()
                    .first()
                currentOnEpisodeChanged(episode)
            }
    }

    // 재생 완료 시 다음 페이지로 이동.
    //
    // 기준은 재생을 거는 쪽과 같은 settledPage 다. currentPage 는 스와이프가 절반을 넘는
    // 순간 뒤집히므로, 넘기는 도중에 클립이 끝나면 아직 재생해 보지도 않은 다음 클립을
    // 건너뛰고 그 다음으로 보내 버린다.
    // 키에 `progress.episodeId` 를 함께 둔다. playback 만으로는 **재무장이 안 되는** 경우가
    // 있다. 다음 클립을 올리면 media3 는 반드시 BUFFERING 을 한 번 발행하지만
    // (ExoPlayerImpl.setMediaSourcesInternal 바이트코드 확인), 그 값을 화면이 본다는 보장이
    // 없다. 화면이 멈춰 있는 동안(예: 백그라운드로 나가 프레임 클록이 서 있는 사이)에 오간
    // 상태는 중간값이 남지 않고, 돌아왔을 때 화면이 보는 것은 ENDED → ENDED 다. 그러면 키가
    // 그대로라 이펙트가 영영 다시 뜨지 않고 자동 넘김이 세션 내내 죽는다.
    // 그때도 끝난 에피소드는 바뀌므로 episodeId 가 재무장의 두 번째 손잡이가 된다.
    LaunchedEffect(playback, progress.episodeId) {
        if (playback != Playback.ENDED) return@LaunchedEffect

        // 소리 한 번 없이 끝난 클립으로는 넘기지 않는다.
        //
        // 잘라낸 창의 시작이 **실제 오디오 길이** 를 넘으면 media3 는 예외를 던지지 않는다.
        // `ClippingMediaSource` 는 `startUs = endUs` 로 창을 접어 길이 0 으로 만들고, 그 창은
        // 재생하자마자 ENDED 가 된다. 그 ENDED 가 여기서 다음 장으로 넘기면, 그런 항목이
        // 이어질 때 목록을 소리 없이 훑고 지나간다.
        //
        // 이것을 [Episode.hasClip] 에서 막을 수는 없다. 거기서 견줄 수 있는 것은 피드가 말한
        // 길이뿐이고 그 값은 실제와 양방향으로 어긋난다 — 짧게 말하면 멀쩡한 클립을 떨어뜨리고,
        // 길게 말하면 이 창을 그대로 통과시킨다. 그래서 **재생해 본 결과** 로 가른다.
        //
        // 근거는 position 이다. progressUpdater 는 `player.duration` 이 양수일 때만 발행하므로
        // 접힌 창은 position 이 0 에 머문다. 정상 클립은 0.5초마다 갱신되어 ENDED 시점에는
        // 이미 0 보다 크다(목록에 오르는 클립은 1초 이상이다 — SoundbiteDao 가 걸러낸다).
        if (!progress.position.isPositive()) return@LaunchedEffect

        // 사용자가 움직이는 중이면 그 손을 이기지 않는다. 멎기를 기다린다.
        snapshotFlow { pagerState.isScrollInProgress }.first { !it }

        // **끝난 그 클립이 아직 이 자리에 있을 때만** 넘긴다.
        //
        // 멎기를 기다리는 것만으로는 모자란다. `settledPage` 는 파생값이라
        // (foundation 1.10.0 바이트코드: `if (isScrollInProgress) settledPageState else
        // currentPage`) 대기가 풀리는 그 스냅샷에서 **이미 사용자가 착지한 페이지** 다.
        // (그래서 이 자리에서는 settledPage 와 currentPage 가 같은 값이다. settledPage 를
        // 쓰는 것은 "멎은 자리" 라는 뜻을 드러내기 위해서지 값이 달라서가 아니다.)
        // 그 값에서 무턱대고 한 칸 더 가면, 사용자가 방금 고른 클립을 듣지도 않고 건너뛴다.
        // "그 사이 재생이 시작되면 이펙트가 취소된다" 에 기대서도 안 된다 — 그 취소는
        // 요청→ViewModel→플레이어→StateFlow→재구성을 거쳐야 해서 대기가 풀리는 같은
        // 스냅샷을 이기지 못한다.
        //
        // 같은 조건이 탭을 다시 열 때도 쓰인다. 클립 플레이어는 싱글턴이라 지난번의
        // (ENDED, position > 0) 이 그대로 남아 있어, 들어오자마자 이펙트가 돌 수 있다.
        // 그것이 지금 보고 있는 클립의 것이 아니면 넘기지 않는다.
        //
        // 다만 **재진입을 통째로 막아 주지는 못한다.** 멎어 있는 페이지의 클립이 마침 그때
        // 끝난 클립이면 "정상 종료" 와 구분할 근거가 없다. (페이저 위치는
        // `rememberPagerState` 가 `rememberSaveable` 로 복원하므로 늘 0 페이지로 돌아오지도
        // 않는다 — foundation 1.10.0 바이트코드 확인.)
        //
        // 판정 근거는 카드가 "자기 차례" 를 가르는 것과 같은 `progress.episodeId` 다.
        //
        // **`progress` 는 한 번 뜬 이펙트 안에서 붙잡힌 값이다.** 목록·콜백처럼
        // rememberUpdatedState 로 감싸지 마라 — 대기 뒤에 읽는 이 소유권 판정이 그 사이
        // 도착한 값으로 뒤집힌다. 여기서 알고 싶은 것은 "지금 무엇이 재생 중인가" 가 아니라
        // "무엇이 끝났는가" 이므로 그 순간의 값이 정답이다.
        //
        // 위에서 episodeId 를 **키** 에 넣은 것은 이것과 어긋나지 않는다. 키가 바뀌면 이펙트가
        // 통째로 다시 떠서 새로 붙잡은 값 하나로 판정한다 — 한 판정 안에서 값이 갈리는 일은
        // 여전히 없다.
        val settledPage = pagerState.settledPage
        val settledEpisode = currentEpisodes.itemSnapshotList.getOrNull(settledPage)
        if (settledEpisode?.id != progress.episodeId) return@LaunchedEffect

        val nextPage = settledPage + 1
        if (nextPage < currentEpisodes.itemCount) {
            try {
                pagerState.animateScrollToPage(nextPage)
            } finally {
                // 애니메이션이 중간에 끊기면 페이저가 **두 페이지 사이에 그대로 남는다.**
                // foundation 1.10.0 의 scroll 에는 되-스냅 경로가 없어(예외 테이블 없음)
                // 취소된 자리에서 멈춘다. 여기서 가장 가까운 페이지로 붙여 준다.
                //
                // **무엇을 고치고 무엇을 못 고치는지 분명히 해 둔다** — 재려 봤다.
                //  - 화면에 머문 채 키가 바뀌어 취소된 경우(재생 버튼을 누르면 playback 이
                //    바뀐다): 고친다. 여기서 오프셋이 0 으로 돌아간다.
                //  - 사용자 드래그가 가로챈 경우: 이 호출이 뮤텍스를 못 잡아 거절된다.
                //    그래도 괜찮다 — 드래그가 끝나며 fling 이 스스로 스냅한다.
                //  - 애니메이션 도중 화면을 떠난 경우: **못 고친다.** 어긋난 오프셋은
                //    컴포지션이 사라지는 그 자리에서 rememberSaveable 이 먼저 저장하고,
                //    취소 재개는 그보다 뒤에 디스패치된다. 여기서 고쳐 봐야 이미 떨어져 나간
                //    페이저만 손보게 된다. 되돌아오면 그 어긋난 값이 복원된다.
                //
                // 거절될 수 있으므로 결과를 삼킨다. 그러지 않으면 드래그 경합의
                // CancellationException 이 이 블록 밖으로 새어 나가고, 그 뒤에 정리 코드를
                // 한 줄이라도 더하면 드래그 때마다 조용히 건너뛰게 된다.
                withContext(NonCancellable) {
                    runCatching {
                        if (pagerState.currentPageOffsetFraction != 0f) {
                            pagerState.scrollToPage(pagerState.currentPage)
                        }
                    }
                }
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
    val clips = remember {
        episodeTestDataList.map {
            it.copy(
                clipStartTime = Instant.fromEpochMilliseconds(60_000L),
                clipDuration = 1278.seconds,
            )
        }
    }
    // 흐름도 remember 로 붙든다. 컴포지션 안에서 새로 만들면 재구성마다 새 LazyPagingItems
    // 가 서고, 그 첫 프레임은 itemCount 가 0 이라 미리보기가 스켈레톤과 본문 사이를 오간다.
    val episodes = remember(clips) { flowOf(PagingData.from(clips)) }
    EpisodiveTheme {
        ClipScreen(
            episodes = episodes,
            playback = Playback.READY,
            progress = Progress(
                position = 278.seconds,
                buffered = 1278.seconds,
                // 클립 길이와 같아야 한다. 다른 값을 두면 미리보기가 실제 화면에서는 나올 수
                // 없는 남은 시간을 보여준다 — 이 변경이 없애려는 바로 그 어긋남이다.
                duration = 1278.seconds,
                // 재생 중인 카드를 보려면 progress 가 그 카드의 것이어야 한다. 빠뜨리면
                // isPlaying = true 를 줘도 멈춘 카드가 그려진다.
                episodeId = clips.first().id,
            ),
            isPlaying = true,
        )
    }
}