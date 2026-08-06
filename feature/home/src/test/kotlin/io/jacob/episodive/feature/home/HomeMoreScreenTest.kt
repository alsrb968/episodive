package io.jacob.episodive.feature.home

import android.content.Context
import android.provider.Settings
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.testing.model.channelTestDataList
import io.jacob.episodive.core.testing.model.liveEpisodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.feature.home.navigation.HomeSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

    /** 빈 목록을 "로딩 중"이 아니라 "결과 없음"으로 판정시키려면 끝에 닿았음을 알려야 한다. */
    private fun endOfPaginationLoadStates() = androidx.paging.LoadStates(
        refresh = androidx.paging.LoadState.NotLoading(endOfPaginationReached = true),
        prepend = androidx.paging.LoadState.NotLoading(endOfPaginationReached = true),
        append = androidx.paging.LoadState.NotLoading(endOfPaginationReached = true),
    )
}
