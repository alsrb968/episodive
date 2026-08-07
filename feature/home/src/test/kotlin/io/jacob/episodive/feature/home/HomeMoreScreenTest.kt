package io.jacob.episodive.feature.home

import android.content.Context
import android.provider.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeUp
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.testing.model.channelTestDataList
import io.jacob.episodive.core.testing.model.liveEpisodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.feature.home.navigation.HomeSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeMoreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** 영문 기본 리소스(`feature_home_section_followed_podcasts`)가 만들어내는 문자열. */
    private val followedPodcastsTitle = "Followed podcasts"

    @Before
    fun disableSystemAnimations() {
        // 스켈레톤의 shimmer는 rememberInfiniteTransition 기반이라 애니메이션이 켜진 채로
        // 테스트를 돌리면 waitForIdle()이 영원히 대기한다.
        Settings.Global.putFloat(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            0f,
        )
    }

    private fun setHomeMoreScreen(
        section: HomeSection,
        content: HomeMoreContent,
        onBackClick: () -> Unit = {},
        onPodcastClick: (Long) -> Unit = {},
        onChannelClick: (Long) -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EpisodiveTheme {
                HomeMoreScreen(
                    section = section,
                    content = content,
                    onBackClick = onBackClick,
                    onPodcastClick = onPodcastClick,
                    onChannelClick = onChannelClick,
                    onPlayEpisode = {},
                    onToggleLikedEpisode = {},
                    onToggleSavedEpisode = {},
                    onRetry = onRetry,
                )
            }
        }
    }

    // --- 팟캐스트 그리드 ---

    @Test
    fun podcastSection_showsPodcastTitles() {
        setHomeMoreScreen(
            section = HomeSection.FollowedPodcasts,
            content = HomeMoreContent.PodcastPaging(
                flowOf(PagingData.from(podcastTestDataList))
            ),
        )

        composeTestRule
            .onAllNodesWithText(podcastTestDataList[0].title, substring = true)
            .onFirst()
            .assertExists()
    }

    @Test
    fun podcastSection_emptyList_showsEmptyMessage() {
        setHomeMoreScreen(
            section = HomeSection.FollowedPodcasts,
            content = HomeMoreContent.PodcastPaging(
                flowOf(PagingData.empty(sourceLoadStates = endOfPaginationLoadStates()))
            ),
        )

        composeTestRule.onNodeWithText("Nothing here yet.").assertExists()
    }

    @Test
    fun podcastSection_backAction_invokesCallback() {
        var backClicked = false
        setHomeMoreScreen(
            section = HomeSection.FollowedPodcasts,
            content = HomeMoreContent.PodcastPaging(
                flowOf(PagingData.from(podcastTestDataList))
            ),
            onBackClick = { backClicked = true },
        )

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }

    // --- 에피소드 리스트 ---

    @Test
    fun episodeSection_showsEpisodeTitles() {
        setHomeMoreScreen(
            section = HomeSection.LiveEpisodes,
            content = HomeMoreContent.EpisodePaging(
                flowOf(PagingData.from(liveEpisodeTestDataList))
            ),
        )

        composeTestRule
            .onAllNodesWithText(liveEpisodeTestDataList[0].title, substring = true)
            .onFirst()
            .assertExists()
    }

    @Test
    fun episodeSection_emptyList_showsEmptyMessage() {
        setHomeMoreScreen(
            section = HomeSection.LiveEpisodes,
            content = HomeMoreContent.EpisodePaging(
                flowOf(PagingData.empty(sourceLoadStates = endOfPaginationLoadStates()))
            ),
        )

        composeTestRule.onNodeWithText("Nothing here yet.").assertExists()
    }

    // --- 채널 그리드 ---

    @Test
    fun channelSection_success_showsChannels() {
        setHomeMoreScreen(
            section = HomeSection.Channels,
            content = HomeMoreContent.ChannelList(
                MutableStateFlow(HomeMoreChannelState.Success(channelTestDataList))
            ),
        )

        composeTestRule
            .onAllNodesWithText(channelTestDataList[0].description, substring = true)
            .onFirst()
            .assertExists()
    }

    @Test
    fun channelSection_error_showsRetryAction() {
        // 채널은 Paging 이 아니라 자체 상태를 쓰므로 재시도도 화면이 직접 처리한다.
        var retried = false
        setHomeMoreScreen(
            section = HomeSection.Channels,
            content = HomeMoreContent.ChannelList(
                MutableStateFlow(
                    HomeMoreChannelState.Error(io.jacob.episodive.core.model.DataError.Offline)
                )
            ),
            onRetry = { retried = true },
        )

        composeTestRule.onNodeWithText("Try again").performClick()

        assertTrue(retried)
    }

    @Test
    fun channelSection_clickChannel_invokesCallback() {
        var clickedId: Long? = null
        setHomeMoreScreen(
            section = HomeSection.Channels,
            content = HomeMoreContent.ChannelList(
                MutableStateFlow(HomeMoreChannelState.Success(channelTestDataList))
            ),
            onChannelClick = { clickedId = it },
        )

        composeTestRule
            .onAllNodesWithText(channelTestDataList[0].description, substring = true)
            .onFirst()
            .performClick()

        assertTrue(clickedId != null)
    }

    // --- 제목 ---

    @Test
    fun section_showsItsOwnTitle() {
        // 홈 섹션 제목을 그대로 쓴다 — 같은 목록에 다른 이름을 붙이면 어디서 왔는지 알기 어렵다.
        setHomeMoreScreen(
            section = HomeSection.LiveEpisodes,
            content = HomeMoreContent.EpisodePaging(
                flowOf(PagingData.empty(sourceLoadStates = endOfPaginationLoadStates()))
            ),
        )

        composeTestRule.onNodeWithText("Live episodes").assertExists()
    }

    @Test
    fun scrollingDown_hidesTitle_butKeepsBackAction() {
        setHomeMoreScreen(
            section = HomeSection.FollowedPodcasts,
            content = HomeMoreContent.PodcastPaging(flowOf(PagingData.from(scrollablePodcasts()))),
        )

        composeTestRule.onNodeWithText(followedPodcastsTitle).assertExists()

        composeTestRule.onRoot().performTouchInput { swipeUp() }

        composeTestRule.onNodeWithText(followedPodcastsTitle).assertDoesNotExist()
        // 나가는 길은 스크롤 위치가 정할 일이 아니다. 제목과 함께 사라지면 사용자는
        // 목록을 위로 되감아야만 나갈 수 있다.
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
    }

    @Test
    fun scrollingUpSlightly_bringsTitleBack() {
        setHomeMoreScreen(
            section = HomeSection.FollowedPodcasts,
            content = HomeMoreContent.PodcastPaging(flowOf(PagingData.from(scrollablePodcasts()))),
        )

        composeTestRule.onRoot().performTouchInput { swipeUp() }
        composeTestRule.onNodeWithText(followedPodcastsTitle).assertDoesNotExist()

        // 맨 위까지 되감지 않는다. 목록 한복판에서 조금 되올린 것만으로 돌아와야 한다 —
        // 위치가 아니라 방향으로 판단한다는 것이 이 동작의 핵심이다.
        composeTestRule.onRoot().performTouchInput {
            swipe(center, center + Offset(0f, ShortScrollBackPx), durationMillis = 200)
        }

        composeTestRule.onNodeWithText(followedPodcastsTitle).assertExists()
    }

    @Test
    fun itemsUnderTheHiddenTitle_areStillClickable() {
        // 겹쳐 그린 헤더가 그 아래 목록의 터치를 삼키면, 제목이 물러난 자리까지 올라온
        // 항목이 눈에는 멀쩡히 보이는데 눌리지 않는 죽은 띠가 된다. M3 TopAppBar 를 그대로
        // 겹치면 실제로 그렇게 된다 — 그쪽이 조건 없이 pointerInput 을 달기 때문이다.
        // 화면 중앙을 쓸어 올리는 다른 테스트들은 이 띠를 건드리지 않아 전부 통과한다.
        var clickedId: Long? = null

        setHomeMoreScreen(
            section = HomeSection.FollowedPodcasts,
            content = HomeMoreContent.PodcastPaging(flowOf(PagingData.from(scrollablePodcasts()))),
            onPodcastClick = { clickedId = it },
        )

        composeTestRule.onRoot().performTouchInput { swipeUp() }
        composeTestRule.onNodeWithText(followedPodcastsTitle).assertDoesNotExist()

        val headerBandCenterY = with(composeTestRule.density) { CollapsedBandCenter.dp.toPx() }
        composeTestRule.onRoot().performTouchInput {
            click(Offset(center.x, headerBandCenterY))
        }

        assertNotNull("제목이 물러난 자리의 항목이 눌리지 않았다", clickedId)
    }

    /**
     * 한 화면을 넘겨 스크롤이 가능할 만큼의 목록.
     *
     * 팩토리 항목이 열 개뿐이라 3열 그리드에서는 화면을 다 채우지 못한다. id 만 새로 주는
     * 이유는 LazyGrid 키가 중복되면 그 자리에서 터지기 때문이다.
     */
    private fun scrollablePodcasts() = List(60) { index ->
        podcastTestDataList[index % podcastTestDataList.size].copy(id = index.toLong())
    }

    /** 빈 목록을 "로딩 중"이 아니라 "결과 없음"으로 판정시키려면 끝에 닿았음을 알려야 한다. */
    private fun endOfPaginationLoadStates() = androidx.paging.LoadStates(
        refresh = androidx.paging.LoadState.NotLoading(endOfPaginationReached = true),
        prepend = androidx.paging.LoadState.NotLoading(endOfPaginationReached = true),
        append = androidx.paging.LoadState.NotLoading(endOfPaginationReached = true),
    )
}

/** 제목을 되돌리는 임계값(8dp)은 넘되, 맨 위까지 되감지는 않을 만큼의 되올림. */
private const val ShortScrollBackPx = 120f

/**
 * 겹친 제목 띠의 세로 중앙(dp).
 *
 * 띠 높이 64dp 의 절반이다. Robolectric 은 상태바 인셋이 0 이라 띠가 화면 맨 위에서
 * 시작한다. 뒤로가기 버튼(좌측 44dp)을 피하려고 가로는 화면 중앙을 쓴다.
 */
private const val CollapsedBandCenter = 32
