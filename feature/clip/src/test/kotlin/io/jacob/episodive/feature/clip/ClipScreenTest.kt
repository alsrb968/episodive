package io.jacob.episodive.feature.clip

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import io.jacob.episodive.core.designsystem.component.SkeletonDefaults
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Playback
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.model.mapper.toHumanReadable
import io.jacob.episodive.core.testing.model.episodeTestDataList
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClipScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun disableSystemAnimations() {
        // 스켈레톤의 shimmer는 rememberInfiniteTransition 기반이라 애니메이션이 켜진 채로
        // 테스트를 돌리면 waitForIdle()이 영원히 대기한다. SkeletonDefaults.shimmerEnabled()가
        // 이 값을 읽으므로 0으로 두면 shimmer 자체가 꺼진다.
        Settings.Global.putFloat(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            0f,
        )
    }

    private val clipEpisodes = episodeTestDataList.map {
        it.copy(
            clipStartTime = Instant.fromEpochMilliseconds(60_000L),
            clipDuration = 1278.seconds,
        )
    }

    private fun setClipScreen(
        episodes: List<Episode> = clipEpisodes,
        playback: Playback = Playback.READY,
        // episodeId 를 빠뜨리면 어떤 카드도 "자기 차례" 가 되지 못해, isPlaying = true 를
        // 줘도 전부 멈춘 카드로 그려진다 — 재생 상태를 보는 테스트가 통째로 무의미해진다.
        progress: Progress = Progress(
            position = 1000.seconds,
            buffered = 1278.seconds,
            duration = 2000.seconds,
            episodeId = clipEpisodes.first().id,
        ),
        isPlaying: Boolean = true,
        onEpisodeChanged: (Episode) -> Unit = {},
        onEpisodeClick: (Episode) -> Unit = {},
        onToggleLikedEpisode: (Episode) -> Unit = {},
        onPodcastClick: (Long) -> Unit = {},
    ) {
        composeTestRule.setContent {
            EpisodiveTheme {
                ClipScreen(
                    episodes = flowOf(PagingData.from(episodes)),
                    playback = playback,
                    progress = progress,
                    isPlaying = isPlaying,
                    onEpisodeChanged = onEpisodeChanged,
                    onEpisodeClick = onEpisodeClick,
                    onToggleLikedEpisode = onToggleLikedEpisode,
                    onPodcastClick = onPodcastClick,
                )
            }
        }
    }

    /** loadState를 직접 통제해야 하는 테스트용 — setClipScreen은 항상 완료된 상태만 만든다. */
    private fun setClipScreenWithPagingData(pagingData: PagingData<Episode>) {
        composeTestRule.setContent {
            EpisodiveTheme {
                ClipScreen(
                    episodes = flowOf(pagingData),
                    playback = Playback.IDLE,
                    progress = Progress(0.seconds, 0.seconds, 0.seconds),
                    isPlaying = false,
                )
            }
        }
    }

    @Test
    fun whenEpisodesExist_clipItemsAreRendered() {
        setClipScreen()

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenEpisodesEmpty_noClipItemsShown() {
        setClipScreen(
            episodes = emptyList(),
            playback = Playback.IDLE,
            progress = Progress(0.seconds, 0.seconds, 0.seconds),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(episodeTestDataList.first().title, substring = true)
            .assertDoesNotExist()
    }

    // --- New: refreshPhase 분기 — itemCount만 보면 결과 0건/실패에서 스피너가 영원히 도는
    // 버그의 재발 방지선 ---

    @Test
    fun whenResultsEmptyAndRefreshComplete_emptyMessageShownWithoutSkeleton() {
        setClipScreenWithPagingData(
            PagingData.from(
                emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = true),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
            )
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.feature_clip_empty))
            .assertExists()
        composeTestRule.onNodeWithTag(SkeletonDefaults.TEST_TAG)
            .assertDoesNotExist()
    }

    @Test
    fun whenRefreshLoading_skeletonShown() {
        setClipScreenWithPagingData(
            PagingData.from(
                emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
            )
        )

        composeTestRule.onNodeWithTag(SkeletonDefaults.TEST_TAG)
            .assertExists()
    }

    @Test
    fun whenMultipleEpisodesExist_firstClipIsRendered() {
        setClipScreen(episodes = clipEpisodes.take(5))

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenPlaybackEnded_clipItemsStillShown() {
        setClipScreen(
            episodes = clipEpisodes.take(3),
            playback = Playback.ENDED,
            progress = Progress(1278.seconds, 1278.seconds, 1278.seconds),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenPlaybackIdle_clipItemsShown() {
        setClipScreen(
            episodes = clipEpisodes.take(3),
            playback = Playback.IDLE,
            progress = Progress(0.seconds, 0.seconds, 0.seconds),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenPlaybackBuffering_clipItemsStillShown() {
        setClipScreen(
            episodes = clipEpisodes.take(3),
            playback = Playback.BUFFERING,
            progress = Progress(0.seconds, 0.seconds, 1278.seconds),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenMultipleEpisodesExist_pagerShowsClipItems() {
        setClipScreen(
            episodes = clipEpisodes.take(5),
            progress = Progress(500.seconds, 1000.seconds, 1278.seconds),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenSingleClipEpisode_titleIsDisplayed() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            progress = Progress(100.seconds, 500.seconds, 1278.seconds),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    // --- New: Callback tests ---

    @Test
    fun clipItem_likeButtonExists() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
        )

        composeTestRule.onAllNodesWithContentDescription("Like")
            .onFirst()
            .assertExists()
    }

    @Test
    fun onEpisodeClick_clickOnClipItemInvokesCallback() {
        var clickedEpisode: Episode? = null
        setClipScreen(
            episodes = clipEpisodes.take(1),
            onEpisodeClick = { clickedEpisode = it },
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .performClick()

        assert(clickedEpisode != null)
    }

    // --- New: Playing state variations ---

    @Test
    fun whenNotPlaying_clipItemStillRendered() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenPlaying_clipItemRendered() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            isPlaying = true,
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    // --- New: Description is shown ---

    @Test
    fun clipEpisode_descriptionIsDisplayed() {
        setClipScreen(episodes = clipEpisodes.take(1))

        // Episode description is rendered via HtmlTextContainer
        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertIsDisplayed()
    }

    // --- New: Different progress values ---

    @Test
    fun zeroProgress_clipItemStillRendered() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            progress = Progress(0.seconds, 0.seconds, 1278.seconds),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun fullProgress_clipItemStillRendered() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            progress = Progress(1278.seconds, 1278.seconds, 1278.seconds),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    // --- New: Play button click ---

    @Test
    fun playButton_clickInvokesOnEpisodeChanged() {
        var changedEpisode: Episode? = null
        setClipScreen(
            episodes = clipEpisodes.take(1),
            isPlaying = false,
            onEpisodeChanged = { changedEpisode = it },
        )

        // The play button is an IconToggleButton with "Play" content description
        composeTestRule.onNodeWithContentDescription("Play")
            .performClick()

        assert(changedEpisode != null)
    }

    // --- New: 카드에 뜨는 시간은 처음부터 그 카드의 클립 시간이어야 한다 ---
    //
    // 카드 하나가 보여주는 시간 텍스트는 remaining 뿐이다. 그 값을 화면 전체가 공유하면
    // 아직 재생 대상이 아닌 카드가 남의 진행 시간(또는 최초 진입의 0초)을 그리고, 자기
    // 차례가 오는 순간 숫자가 튄다.

    @Test
    fun whenNothingIsPlayingYet_cardShowsItsOwnClipDuration() {
        // 첫 진입: 아직 어떤 클립도 플레이어에 올라가지 않아 progress 에 episodeId 가 없다.
        // 이때 progress.remaining 을 그대로 쓰면 0초가 뜬다.
        val episode = clipEpisodes.first()
        setClipScreen(
            episodes = listOf(episode),
            playback = Playback.IDLE,
            progress = Progress(0.seconds, 0.seconds, 0.seconds),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(episode.clipPlaybackDuration.toHumanReadable())
            .assertExists()
    }

    @Test
    fun whenProgressBelongsToAnotherEpisode_cardShowsItsOwnClipDuration() {
        // 스와이프 직후처럼 progress 가 아직 이전 클립의 것일 때. 그 값을 빌려 쓰면
        // 이 카드에 엉뚱한 에피소드의 남은 시간이 뜬다.
        val episode = clipEpisodes.first()
        setClipScreen(
            episodes = listOf(episode),
            progress = Progress(
                position = 300.seconds,
                buffered = 300.seconds,
                duration = 7200.seconds,
                episodeId = episode.id + 1,
            ),
        )

        composeTestRule.onNodeWithText(episode.clipPlaybackDuration.toHumanReadable())
            .assertExists()
        // 남의 남은 시간(6900초)이 새어 나오지 않아야 한다.
        composeTestRule.onNodeWithText(6900.seconds.toHumanReadable())
            .assertDoesNotExist()
    }

    @Test
    fun whenProgressBelongsToThisEpisode_cardShowsTheRemainingTime() {
        // 자기 차례인 카드만 실시간으로 흐른다.
        val episode = clipEpisodes.first()
        setClipScreen(
            episodes = listOf(episode),
            progress = Progress(
                position = 278.seconds,
                buffered = 278.seconds,
                duration = 1278.seconds,
                episodeId = episode.id,
            ),
        )

        composeTestRule.onNodeWithText(1000.seconds.toHumanReadable())
            .assertExists()
    }

    // --- New: 첫 클립은 한 번만 올린다 ---
    //
    // "첫 클립 자동 재생" 이펙트와 settledPage 콜렉터가 둘 다 page 0 에 발화하면 같은 클립에
    // setMediaItem 이 두 번 걸리고, 두 번째가 방금 시작한 재생을 처음으로 되돌린다.

    @Test
    fun onFirstEntry_theFirstClipIsRequestedExactlyOnce() {
        val requested = mutableListOf<Episode>()
        setClipScreen(
            episodes = clipEpisodes.take(3),
            playback = Playback.IDLE,
            progress = Progress(0.seconds, 0.seconds, 0.seconds),
            isPlaying = false,
            onEpisodeChanged = { requested.add(it) },
        )

        composeTestRule.waitForIdle()

        assertEquals(listOf(clipEpisodes.first().id), requested.map { it.id })
    }

    @Test
    fun whenTheSameEpisodeIsReemittedWithUpdatedFlags_theClipIsNotRestarted() {
        // 좋아요를 누르면 Paging 이 같은 에피소드를 새 인스턴스로 다시 흘린다. 그때 재생을
        // 다시 걸면 듣던 자리가 사라진다 — id 가 같으면 같은 클립으로 봐야 한다.
        //
        // 재방출을 UI 클릭으로 일으키지 않고 흐름에 직접 흘려 넣는다. 클릭에 기대면 실제로
        // 토글이 걸렸는지가 이 테스트의 전제가 되어, 클릭이 빗나가는 순간 아무것도 검증하지
        // 않으면서 초록으로 남는다. 그리고 흐름은 remember 로 붙든다 — 컴포지션마다 새로
        // 만들면 collectAsLazyPagingItems 가 매번 새 LazyPagingItems 를 세워 페이저 자체가
        // 헐렸다 다시 서므로, 재방출이 아니라 재생성을 시험하게 된다.
        val episode = clipEpisodes.first()
        val requested = mutableListOf<Episode>()
        lateinit var pagingFlow: MutableStateFlow<PagingData<Episode>>

        composeTestRule.setContent {
            val flow = remember {
                MutableStateFlow(PagingData.from(listOf(episode)))
            }
            pagingFlow = flow
            EpisodiveTheme {
                ClipScreen(
                    episodes = flow,
                    playback = Playback.READY,
                    progress = Progress(
                        position = 100.seconds,
                        buffered = 100.seconds,
                        duration = 1278.seconds,
                        episodeId = episode.id,
                    ),
                    isPlaying = true,
                    onEpisodeChanged = { requested.add(it) },
                )
            }
        }
        composeTestRule.waitForIdle()
        assertEquals(1, requested.size)

        // 같은 에피소드가 좋아요만 달라진 새 인스턴스로 다시 흘러온다.
        pagingFlow.value = PagingData.from(listOf(episode.copy(likedAt = episode.datePublished)))
        composeTestRule.waitForIdle()

        assertEquals(1, requested.size)
    }

    @Test
    fun whenTheSettledPageFallsOutsideAShrunkList_theClipTabDoesNotCrash() {
        // 목록이 갱신되어 짧아지면 그 순간 settledPage 가 범위 밖에 남는다. 그 자리를
        // LazyPagingItems.get 으로 읽으면 IndexOutOfBoundsException 이 그대로 터진다.
        val requested = mutableListOf<Episode>()
        lateinit var pagingFlow: MutableStateFlow<PagingData<Episode>>

        composeTestRule.setContent {
            val flow = remember {
                MutableStateFlow(PagingData.from(clipEpisodes.take(5)))
            }
            pagingFlow = flow
            EpisodiveTheme {
                ClipScreen(
                    episodes = flow,
                    playback = Playback.READY,
                    progress = Progress(0.seconds, 0.seconds, 0.seconds),
                    isPlaying = false,
                    onEpisodeChanged = { requested.add(it) },
                )
            }
        }
        composeTestRule.waitForIdle()

        // 뒤쪽 페이지로 실제로 넘겨 settledPage 를 0 이 아닌 값으로 만든다. 여기를 건너뛰면
        // 목록을 줄여도 settledPage 가 0 이라 범위 안이고, 잡으려는 조건이 만들어지지 않는다.
        repeat(2) {
            composeTestRule.onRoot().performTouchInput { swipeUp() }
            composeTestRule.waitForIdle()
        }
        val settledBeyondShrunkList = requested.size > 1
        check(settledBeyondShrunkList) {
            "스와이프가 페이지를 넘기지 못했다 — 이 테스트는 settledPage > 0 을 전제한다"
        }

        // 5개 → 1개. 앞서 자리잡은 페이지 번호가 새 목록에는 없다.
        pagingFlow.value = PagingData.from(listOf(clipEpisodes.first()))
        composeTestRule.waitForIdle()

        // 여기까지 예외 없이 왔으면 통과.
    }

    @Test
    fun whenEpisodeHasNoClipMetadata_cardFallsBackToTheEpisodeDuration() {
        // 클립 정보가 없으면 플레이어도 잘라 올리지 않으므로 전체 길이가 실제 재생 길이다.
        val whole = episodeTestDataList.first()
        check(!whole.hasClip) { "이 테스트는 클립 메타가 없는 데이터를 전제한다" }
        val duration = requireNotNull(whole.duration) { "테스트 데이터에 duration 이 필요하다" }

        setClipScreen(
            episodes = listOf(whole),
            playback = Playback.IDLE,
            progress = Progress(0.seconds, 0.seconds, 0.seconds),
            isPlaying = false,
        )

        composeTestRule.onNodeWithText(duration.toHumanReadable())
            .assertExists()
    }
}
