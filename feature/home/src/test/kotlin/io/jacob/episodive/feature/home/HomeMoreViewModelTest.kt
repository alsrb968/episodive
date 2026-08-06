package io.jacob.episodive.feature.home

import app.cash.turbine.test
import io.jacob.episodive.core.domain.usecase.channel.GetChannelsUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLiveEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetMyRandomEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.SaveEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetForeignTrendingPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetLocalTrendingPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserTrendingPodcastsPagingUseCase
import io.jacob.episodive.core.testing.model.channelTestDataList
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.jacob.episodive.feature.home.navigation.HomeSection
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeMoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUserRecentPodcastsPagingUseCase =
        mockk<GetUserRecentPodcastsPagingUseCase>(relaxed = true)
    private val getMyRandomEpisodesPagingUseCase =
        mockk<GetMyRandomEpisodesPagingUseCase>(relaxed = true)
    private val getUserTrendingPodcastsPagingUseCase =
        mockk<GetUserTrendingPodcastsPagingUseCase>(relaxed = true)
    private val getFollowedPodcastsPagingUseCase =
        mockk<GetFollowedPodcastsPagingUseCase>(relaxed = true)
    private val getLocalTrendingPodcastsPagingUseCase =
        mockk<GetLocalTrendingPodcastsPagingUseCase>(relaxed = true)
    private val getForeignTrendingPodcastsPagingUseCase =
        mockk<GetForeignTrendingPodcastsPagingUseCase>(relaxed = true)
    private val getLiveEpisodesPagingUseCase =
        mockk<GetLiveEpisodesPagingUseCase>(relaxed = true)
    private val getChannelsUseCase = mockk<GetChannelsUseCase>(relaxed = true)
    private val playEpisodeUseCase = mockk<PlayEpisodeUseCase>(relaxed = true)
    private val toggleLikedEpisodeUseCase = mockk<ToggleLikedEpisodeUseCase>(relaxed = true)
    private val saveEpisodeUseCase = mockk<SaveEpisodeUseCase>(relaxed = true)

    private fun createViewModel(section: HomeSection) = HomeMoreViewModel(
        getUserRecentPodcastsPagingUseCase = getUserRecentPodcastsPagingUseCase,
        getMyRandomEpisodesPagingUseCase = getMyRandomEpisodesPagingUseCase,
        getUserTrendingPodcastsPagingUseCase = getUserTrendingPodcastsPagingUseCase,
        getFollowedPodcastsPagingUseCase = getFollowedPodcastsPagingUseCase,
        getLocalTrendingPodcastsPagingUseCase = getLocalTrendingPodcastsPagingUseCase,
        getForeignTrendingPodcastsPagingUseCase = getForeignTrendingPodcastsPagingUseCase,
        getLiveEpisodesPagingUseCase = getLiveEpisodesPagingUseCase,
        getChannelsUseCase = getChannelsUseCase,
        playEpisodeUseCase = playEpisodeUseCase,
        toggleLikedEpisodeUseCase = toggleLikedEpisodeUseCase,
        saveEpisodeUseCase = saveEpisodeUseCase,
        section = section,
    )

    @After
    fun teardown() {
        confirmVerified(
            getUserRecentPodcastsPagingUseCase,
            getMyRandomEpisodesPagingUseCase,
            getUserTrendingPodcastsPagingUseCase,
            getFollowedPodcastsPagingUseCase,
            getLocalTrendingPodcastsPagingUseCase,
            getForeignTrendingPodcastsPagingUseCase,
            getLiveEpisodesPagingUseCase,
            getChannelsUseCase,
        )
    }

    // --- 섹션 → 데이터 소스 배선 ---
    // confirmVerified 로 "다른 유스케이스는 부르지 않았다"까지 확인한다. 배선이 어긋나면
    // 사용자는 엉뚱한 목록을 보게 되는데, 화면만 봐서는 어느 섹션의 데이터인지 알기 어렵다.

    @Test
    fun `Given MyRecentPodcasts section, When created, Then only recent paging is used`() {
        createViewModel(HomeSection.MyRecentPodcasts)

        verify { getUserRecentPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given RandomEpisodes section, When created, Then only random paging is used`() {
        createViewModel(HomeSection.RandomEpisodes)

        verify { getMyRandomEpisodesPagingUseCase(any()) }
    }

    @Test
    fun `Given MyTrendingPodcasts section, When created, Then only trending paging is used`() {
        createViewModel(HomeSection.MyTrendingPodcasts)

        verify { getUserTrendingPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given FollowedPodcasts section, When created, Then only followed paging is used`() {
        createViewModel(HomeSection.FollowedPodcasts)

        verify { getFollowedPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given LocalTrendingPodcasts section, When created, Then only local trending paging is used`() {
        createViewModel(HomeSection.LocalTrendingPodcasts)

        verify { getLocalTrendingPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given ForeignTrendingPodcasts section, When created, Then only foreign trending paging is used`() {
        createViewModel(HomeSection.ForeignTrendingPodcasts)

        verify { getForeignTrendingPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given LiveEpisodes section, When created, Then only live paging is used`() {
        createViewModel(HomeSection.LiveEpisodes)

        verify { getLiveEpisodesPagingUseCase(any()) }
    }

    // 채널만 배선 확인 방식이 다르다. WhileSubscribed 로 공유하는 상태 흐름이라 구독자가
    // 붙기 전에는 유스케이스가 호출되지 않는다 — 아래 상태 테스트가 실제 호출까지 확인한다.

    // --- 콘텐츠 종류 ---

    @Test
    fun `Given podcast section, When created, Then content is podcast paging`() {
        val viewModel = createViewModel(HomeSection.FollowedPodcasts)

        assertTrue(viewModel.content is HomeMoreContent.PodcastPaging)

        verify { getFollowedPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given episode section, When created, Then content is episode paging`() {
        val viewModel = createViewModel(HomeSection.LiveEpisodes)

        assertTrue(viewModel.content is HomeMoreContent.EpisodePaging)

        verify { getLiveEpisodesPagingUseCase(any()) }
    }

    @Test
    fun `Given Channels section, When created, Then content is channel list`() {
        // 채널은 Paging 소스가 없어 일반 상태 흐름을 쓴다.
        every { getChannelsUseCase() } returns flowOf(channelTestDataList)

        val viewModel = createViewModel(HomeSection.Channels)

        assertTrue(viewModel.content is HomeMoreContent.ChannelList)
    }

    // --- 채널 상태 ---

    @Test
    fun `Given channels load, When state collected, Then Success is emitted`() = runTest {
        every { getChannelsUseCase() } returns flowOf(channelTestDataList)

        val viewModel = createViewModel(HomeSection.Channels)
        val content = viewModel.content as HomeMoreContent.ChannelList

        content.state.test {
            assertEquals(
                HomeMoreChannelState.Success(channelTestDataList),
                awaitItemSkippingLoading(),
            )
        }

        verify { getChannelsUseCase() }
    }

    @Test
    fun `Given channels fail, When state collected, Then Error is emitted`() = runTest {
        every { getChannelsUseCase() } returns flow { throw java.io.IOException("boom") }

        val viewModel = createViewModel(HomeSection.Channels)
        val content = viewModel.content as HomeMoreContent.ChannelList

        content.state.test {
            assertTrue(awaitItemSkippingLoading() is HomeMoreChannelState.Error)
        }

        verify { getChannelsUseCase() }
    }

    // --- Effect ---

    @Test
    fun `Given ClickBack action, When sent, Then NavigateBack effect is emitted`() = runTest {
        val viewModel = createViewModel(HomeSection.FollowedPodcasts)

        viewModel.effect.test {
            viewModel.sendAction(HomeMoreAction.ClickBack)
            assertEquals(HomeMoreEffect.NavigateBack, awaitItem())
        }

        verify { getFollowedPodcastsPagingUseCase(any()) }
    }

    @Test
    fun `Given ClickPodcast action, When sent, Then NavigateToPodcast effect is emitted`() =
        runTest {
            val viewModel = createViewModel(HomeSection.FollowedPodcasts)

            viewModel.effect.test {
                viewModel.sendAction(HomeMoreAction.ClickPodcast(42L))
                assertEquals(HomeMoreEffect.NavigateToPodcast(42L), awaitItem())
            }

            verify { getFollowedPodcastsPagingUseCase(any()) }
        }

    @Test
    fun `Given ClickChannel action, When sent, Then NavigateToChannel effect is emitted`() =
        runTest {
            every { getChannelsUseCase() } returns flowOf(channelTestDataList)
            val viewModel = createViewModel(HomeSection.Channels)

            viewModel.effect.test {
                viewModel.sendAction(HomeMoreAction.ClickChannel(7L))
                assertEquals(HomeMoreEffect.NavigateToChannel(7L), awaitItem())
            }
        }

    @Test
    fun `Given ToggleSavedEpisode removing, When sent, Then unsave snackbar effect is emitted`() =
        runTest {
            // 저장이 해제됐을 때만 실행 취소를 제안한다.
            io.mockk.coEvery { saveEpisodeUseCase(any()) } returns false
            val viewModel = createViewModel(HomeSection.LiveEpisodes)

            viewModel.effect.test {
                viewModel.sendAction(HomeMoreAction.ToggleSavedEpisode(episodeTestData))
                assertEquals(HomeMoreEffect.ShowUnsaveSnackbar(episodeTestData), awaitItem())
            }

            verify { getLiveEpisodesPagingUseCase(any()) }
        }

    /**
     * 초기값 [HomeMoreChannelState.Loading] 을 건너뛰고 실제 결과를 받는다.
     *
     * 상태 흐름의 첫 값이 Loading 이지만, 소스가 즉시 값을 내면 구독이 붙기 전에 이미
     * 결과로 넘어가 있어 Loading 이 관찰되지 않는다. 둘 중 어느 쪽이든 통과해야 한다.
     */
    private suspend fun app.cash.turbine.ReceiveTurbine<HomeMoreChannelState>.awaitItemSkippingLoading(): HomeMoreChannelState {
        val first = awaitItem()
        return if (first is HomeMoreChannelState.Loading) awaitItem() else first
    }
}
