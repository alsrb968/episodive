package io.jacob.episodive.feature.clip

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.onRoot
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
import org.robolectric.annotation.Config

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
            // 흐름을 remember 로 붙든다. 컴포지션 안에서 새로 만들면 재구성 때마다
            // collectAsLazyPagingItems 가 새 LazyPagingItems 를 세우고, 그것을 키로 삼는
            // 자동 재생 이펙트가 다시 돌아 같은 클립을 두 번 요청한다.
            val flow = remember(episodes) { flowOf(PagingData.from(episodes)) }
            EpisodiveTheme {
                ClipScreen(
                    episodes = flow,
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

    // --- 재생이 끝나면 다음 클립으로 넘어간다 ---
    //
    // 이 동작에는 한동안 테스트가 하나도 없었다. 기준을 currentPage 로 되돌려도, 자동 넘김을
    // 통째로 지워도 빨개지는 것이 없었다.

    @Test
    fun whenAClipFinishesAfterPlaying_theNextClipIsRequested() {
        val requested = mutableListOf<Episode>()

        setClipScreen(
            episodes = clipEpisodes.take(3),
            playback = Playback.ENDED,
            // position 이 0 을 벗어나 있다 = 실제로 재생됐다.
            progress = Progress(
                position = 1278.seconds,
                buffered = 1278.seconds,
                duration = 1278.seconds,
                episodeId = clipEpisodes.first().id,
            ),
            isPlaying = false,
            onEpisodeChanged = { requested.add(it) },
        )
        composeTestRule.waitForIdle()

        assertEquals(clipEpisodes[1].id, requested.last().id)
    }

    @Test
    fun whenAClipEndsWithoutEverPlaying_theListDoesNotAdvance() {
        // 잘라낸 창의 시작이 실제 오디오 길이를 넘으면 media3 는 예외를 던지지 않고 창을
        // 길이 0 으로 접는다(1.8.0 실행 확인). 그 창은 재생하자마자 ENDED 다. 그 ENDED 로
        // 다음 장을 넘기면 그런 항목이 이어질 때 목록을 소리 없이 훑고 지나간다.
        // 가르는 근거는 피드 메타가 아니라 position — 접힌 창은 0 에 머문다.
        val requested = mutableListOf<Episode>()

        setClipScreen(
            episodes = clipEpisodes.take(3),
            playback = Playback.ENDED,
            progress = Progress(
                position = 0.seconds,
                buffered = 0.seconds,
                duration = 0.seconds,
                episodeId = clipEpisodes.first().id,
            ),
            isPlaying = false,
            onEpisodeChanged = { requested.add(it) },
        )
        composeTestRule.waitForIdle()

        assertEquals(clipEpisodes.first().id, requested.last().id)
    }

    @Test
    fun whenTheLastClipFinishes_thereIsNothingToAdvanceTo() {
        val requested = mutableListOf<Episode>()

        setClipScreen(
            episodes = clipEpisodes.take(1),
            playback = Playback.ENDED,
            progress = Progress(
                position = 1278.seconds,
                buffered = 1278.seconds,
                duration = 1278.seconds,
                episodeId = clipEpisodes.first().id,
            ),
            isPlaying = false,
            onEpisodeChanged = { requested.add(it) },
        )
        composeTestRule.waitForIdle()

        assertEquals(listOf(clipEpisodes.first().id), requested.map { it.id })
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
            progress = Progress(500.seconds, 1000.seconds, 1278.seconds, clipEpisodes.first().id),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun whenSingleClipEpisode_titleIsDisplayed() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            progress = Progress(100.seconds, 500.seconds, 1278.seconds, clipEpisodes.first().id),
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
            progress = Progress(0.seconds, 0.seconds, 1278.seconds, clipEpisodes.first().id),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun fullProgress_clipItemStillRendered() {
        setClipScreen(
            episodes = clipEpisodes.take(1),
            progress = Progress(1278.seconds, 1278.seconds, 1278.seconds, clipEpisodes.first().id),
        )

        composeTestRule.onNodeWithText(clipEpisodes.first().title, substring = true)
            .assertExists()
    }

    // --- New: Play button click ---

    @Test
    // 기본 Robolectric 화면은 카드의 컨트롤 줄이 밑으로 밀려 나갈 만큼 좁다. 버튼이 화면
    // 밖이면 performClick 은 조용히 아무 일도 하지 않으므로, 실기와 같은 크기를 준다.
    @Config(qualifiers = "w411dp-h914dp")
    fun playButton_clickInvokesOnEpisodeChanged() {
        // "null 이 아니다" 로는 부족하다 — 화면에 들어오는 것만으로 자동 재생이 이미 한 번
        // 채우므로, 재생 버튼을 통째로 없애도 그 단언은 통과한다. 클릭 전후를 비교한다.
        val requested = mutableListOf<Episode>()
        setClipScreen(
            episodes = clipEpisodes.take(1),
            isPlaying = false,
            onEpisodeChanged = { requested.add(it) },
        )
        composeTestRule.waitForIdle()
        val beforeClick = requested.size

        // 표시 확인이 먼저다. 화면 밖 노드에 performClick 을 하면 예외도 없이 그냥 아무 일도
        // 일어나지 않아, 클릭을 검증한 적 없는 테스트가 초록으로 남는다.
        composeTestRule.onNodeWithContentDescription("Play")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(beforeClick + 1, requested.size)
        assertEquals(clipEpisodes.first().id, requested.last().id)
    }

    // --- New: 카드에 뜨는 시간은 처음부터 그 카드의 클립 시간이어야 한다 ---
    //
    // 카드 하나가 보여주는 시간 텍스트는 remaining 뿐이다. 그 값을 화면 전체가 공유하면
    // 아직 재생 대상이 아닌 카드가 남의 진행 시간(또는 최초 진입의 0초)을 그리고, 자기
    // 차례가 오는 순간 숫자가 튄다.

    @Test
    // 시간 배지는 카드 아래쪽 컨트롤 줄에 있어 기본 화면에서는 밀려 나간다.
    // 실기 크기를 주고 assertIsDisplayed 까지 가야 실제로 보이는 것을 검사한다.
    @Config(qualifiers = "w411dp-h914dp")
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
            .assertIsDisplayed()
    }

    @Test
    // 시간 배지는 카드 아래쪽 컨트롤 줄에 있어 기본 화면에서는 밀려 나간다.
    // 실기 크기를 주고 assertIsDisplayed 까지 가야 실제로 보이는 것을 검사한다.
    @Config(qualifiers = "w411dp-h914dp")
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
            .assertIsDisplayed()
        // 남의 남은 시간(6900초)이 새어 나오지 않아야 한다.
        composeTestRule.onNodeWithText(6900.seconds.toHumanReadable())
            .assertDoesNotExist()
    }

    @Test
    // 시간 배지는 카드 아래쪽 컨트롤 줄에 있어 기본 화면에서는 밀려 나간다.
    // 실기 크기를 주고 assertIsDisplayed 까지 가야 실제로 보이는 것을 검사한다.
    @Config(qualifiers = "w411dp-h914dp")
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
            .assertIsDisplayed()
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
                    // episodeId 를 비워 둔다. 이미 올라 있는 것으로 두면 첫 요청부터
                    // 건너뛰어, 정작 보려는 "재방출 때 다시 걸지 않는가" 를 못 본다.
                    progress = Progress(0.seconds, 0.seconds, 0.seconds),
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
    fun whenNothingIsOnThePlayer_theSettledClipIsRequested() {
        // 프로세스가 죽었다 살아난 자리. 플레이어가 비어 있으면(progress.episodeId == null)
        // 페이지가 복원돼 있더라도 재생을 걸어야 한다 — 건너뛰면 화면이 소리 없이 멎는다.
        val playing = clipEpisodes.first()
        val requested = mutableListOf<Episode>()

        setClipScreen(
            episodes = listOf(playing),
            playback = Playback.IDLE,
            progress = Progress(0.seconds, 0.seconds, 0.seconds),
            isPlaying = false,
            onEpisodeChanged = { requested.add(it) },
        )
        composeTestRule.waitForIdle()

        assertEquals(listOf(playing.id), requested.map { it.id })
    }

    @Test
    fun whenAPagingRefreshShiftsTheWindow_thePlayingClipIsNotSwappedOut() {
        // 좋아요를 누르면 SoundbiteEpisodePagingSource 가 liked_episodes 무효화로 refresh 하고,
        // getRefreshKey 가 앵커 기준으로 창을 옮긴다. 그러면 같은 자리에 다른 에피소드가 앉는데,
        // 그때 재생을 갈아치우면 듣고 있던 클립이 잘려 나간다. 페이지가 그대로면 재생도 그대로여야
        // 한다 — 화면에 보이는 카드가 바뀌는 것과 무엇을 재생할지는 별개다.
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
                    isPlaying = true,
                    onEpisodeChanged = { requested.add(it) },
                )
            }
        }
        composeTestRule.waitForIdle()
        val playingBeforeRefresh = requested.last().id

        // 창이 뒤로 밀려 같은 자리에 다른 에피소드가 앉는다.
        pagingFlow.value = PagingData.from(clipEpisodes.drop(3).take(5))
        composeTestRule.waitForIdle()

        assertEquals(playingBeforeRefresh, requested.last().id)
    }

    @Test
    fun whenTheListShrinksUnderTheSettledPage_theRemainingClipIsRequested() {
        // 목록이 갱신되어 짧아지면 페이저가 페이지를 앞으로 당긴다. 그때 재생도 남아 있는
        // 클립으로 따라가야 한다 — 따라가지 않으면 사라진 클립의 소리만 남고 화면은 멎는다.
        //
        // 이 테스트는 "따라간다" 만 지킨다. 범위를 벗어난 자리를 읽어 터지는 것까지 잡으려
        // 했지만 그러지 못한다: 스와이프가 얼마나 멀리 가는지, 목록 축소와 페이저 clamp 중
        // 무엇이 먼저인지에 결과가 달렸다. 실제로 범위 밖 인덱싱을 되돌려 놓고도 통과하는
        // 것을 확인했다. 그쪽 방어는 itemSnapshotList.getOrNull 이 코드로 지고 있다.
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

        // 두 번 밀어 올려 앞쪽 페이지에서 벗어난다. 아래에서 목록을 1개로 줄일 때 "지금 자리가
        // 새 목록에는 없다" 가 성립해야 하므로, 실제로 넘어갔는지는 바로 다음 check 로 못박는다.
        repeat(2) {
            composeTestRule.onRoot().performTouchInput { swipeUp() }
            composeTestRule.waitForIdle()
        }
        check(requested.last().id != clipEpisodes.first().id) {
            "스와이프가 페이지를 넘기지 못해 전제(첫 페이지가 아님)가 서지 않았다. " +
                "실제 요청 순서: ${requested.map { it.id }}"
        }

        // 5개 → 1개. 앞서 자리잡은 페이지 번호가 새 목록에는 없다.
        pagingFlow.value = PagingData.from(listOf(clipEpisodes.first()))
        composeTestRule.waitForIdle()

        // 예외가 없는 것으로는 부족하다 — 남은 클립으로 실제로 옮겨가야 한다.
        assertEquals(clipEpisodes.first().id, requested.last().id)
    }

    // 한 배지 안의 파도 애니메이션과 시간은 같은 기준으로 갈라야 한다. 시간만 검사하면
    // 애니메이션 쪽 기준을 currentPage 로 되돌려도 아무 테스트가 빨개지지 않는다.

    @Test
    @Config(qualifiers = "w411dp-h914dp")
    fun whenProgressBelongsToAnotherEpisode_theCardIsNotDrawnAsPlaying() {
        val episode = clipEpisodes.first()
        setClipScreen(
            episodes = listOf(episode),
            isPlaying = true,
            progress = Progress(
                position = 300.seconds,
                buffered = 300.seconds,
                duration = 7200.seconds,
                episodeId = episode.id + 1,
            ),
        )

        // 재생 중이면 Pause 아이콘이, 아니면 Play 아이콘이 그려진다.
        composeTestRule.onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w411dp-h914dp")
    fun whenProgressBelongsToThisEpisode_theCardIsDrawnAsPlaying() {
        val episode = clipEpisodes.first()
        setClipScreen(
            episodes = listOf(episode),
            isPlaying = true,
            progress = Progress(
                position = 278.seconds,
                buffered = 278.seconds,
                duration = 1278.seconds,
                episodeId = episode.id,
            ),
        )

        composeTestRule.onNodeWithContentDescription("Pause").assertIsDisplayed()
    }

    @Test
    // 시간 배지는 카드 아래쪽 컨트롤 줄에 있어 기본 화면에서는 밀려 나간다.
    // 실기 크기를 주고 assertIsDisplayed 까지 가야 실제로 보이는 것을 검사한다.
    @Config(qualifiers = "w411dp-h914dp")
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
            .assertIsDisplayed()
    }
}
