package io.jacob.episodive.feature.home

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.jacob.episodive.core.domain.usecase.channel.GetChannelsUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLiveEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetMyRandomEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetPlayingEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.SaveEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.ResumeEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetForeignTrendingPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetLocalTrendingPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserTrendingPodcastsUseCase
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.testing.model.channelTestDataList
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coVerify
import io.jacob.episodive.feature.home.navigation.HomeSection
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPlayingEpisodesUseCase = mockk<GetPlayingEpisodesUseCase>(relaxed = true)
    private val getUserRecentPodcastsUseCase = mockk<GetUserRecentPodcastsUseCase>(relaxed = true)
    private val getMyRandomEpisodesUseCase = mockk<GetMyRandomEpisodesUseCase>(relaxed = true)
    private val getUserTrendingPodcastsUseCase =
        mockk<GetUserTrendingPodcastsUseCase>(relaxed = true)
    private val getFollowedPodcastsUseCase = mockk<GetFollowedPodcastsUseCase>(relaxed = true)
    private val getLocalTrendingPodcastsUseCase =
        mockk<GetLocalTrendingPodcastsUseCase>(relaxed = true)
    private val getForeignTrendingPodcastsUseCase =
        mockk<GetForeignTrendingPodcastsUseCase>(relaxed = true)
    private val getLiveEpisodesUseCase = mockk<GetLiveEpisodesUseCase>(relaxed = true)
    private val getChannelsUseCase = mockk<GetChannelsUseCase>(relaxed = true)
    private val playEpisodeUseCase = mockk<PlayEpisodeUseCase>(relaxed = true)
    private val resumeEpisodeUseCase = mockk<ResumeEpisodeUseCase>(relaxed = true)
    private val toggleLikedEpisodeUseCase = mockk<ToggleLikedEpisodeUseCase>(relaxed = true)
    private val saveEpisodeUseCase = mockk<SaveEpisodeUseCase>(relaxed = true)

    private fun setupDefaultMocks() {
        every { getPlayingEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getUserRecentPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getMyRandomEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getUserTrendingPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getLocalTrendingPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getForeignTrendingPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getLiveEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getChannelsUseCase() } returns flowOf(emptyList())
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            getPlayingEpisodesUseCase = getPlayingEpisodesUseCase,
            getUserRecentPodcastsUseCase = getUserRecentPodcastsUseCase,
            getMyRandomEpisodesUseCase = getMyRandomEpisodesUseCase,
            getUserTrendingPodcastsUseCase = getUserTrendingPodcastsUseCase,
            getFollowedPodcastsUseCase = getFollowedPodcastsUseCase,
            getLocalTrendingPodcastsUseCase = getLocalTrendingPodcastsUseCase,
            getForeignTrendingPodcastsUseCase = getForeignTrendingPodcastsUseCase,
            getLiveEpisodesUseCase = getLiveEpisodesUseCase,
            getChannelsUseCase = getChannelsUseCase,
            playEpisodeUseCase = playEpisodeUseCase,
            resumeEpisodeUseCase = resumeEpisodeUseCase,
            toggleLikedEpisodeUseCase = toggleLikedEpisodeUseCase,
            saveEpisodeUseCase = saveEpisodeUseCase,
        )
    }

    @After
    fun teardown() {
        confirmVerified(playEpisodeUseCase, resumeEpisodeUseCase, toggleLikedEpisodeUseCase)
    }

    @Test
    fun `Given no emissions, When ViewModel is created, Then initial state is Loading`() = runTest {
        every { getPlayingEpisodesUseCase(max = any()) } returns flowOf()
        every { getUserRecentPodcastsUseCase(max = any()) } returns flowOf()
        every { getMyRandomEpisodesUseCase(max = any()) } returns flowOf()
        every { getUserTrendingPodcastsUseCase(max = any()) } returns flowOf()
        every { getFollowedPodcastsUseCase(max = any()) } returns flowOf()
        every { getLocalTrendingPodcastsUseCase(max = any()) } returns flowOf()
        every { getForeignTrendingPodcastsUseCase(max = any()) } returns flowOf()
        every { getLiveEpisodesUseCase(max = any()) } returns flowOf()
        every { getChannelsUseCase() } returns flowOf()

        val viewModel = createViewModel()

        assertEquals(HomeState.Loading, viewModel.state.value)
    }

    @Test
    fun `Given all flows emit data, When collecting, Then state is Success with all fields`() =
        runTest {
            val playingEpisodes = episodeTestDataList.take(2)
            val recentPodcasts = podcastTestDataList.take(2)
            val randomEpisodes = episodeTestDataList.take(3)
            val trendingPodcasts = podcastTestDataList.take(2)
            val followedPodcasts = podcastTestDataList.take(1)
            val localTrending = podcastTestDataList.take(2)
            val foreignTrending = podcastTestDataList.take(2)
            val liveEpisodes = episodeTestDataList.take(1)
            val channels = channelTestDataList.take(2)

            every { getPlayingEpisodesUseCase(max = any()) } returns flowOf(playingEpisodes)
            every { getUserRecentPodcastsUseCase(max = any()) } returns flowOf(recentPodcasts)
            every { getMyRandomEpisodesUseCase(max = any()) } returns flowOf(randomEpisodes)
            every { getUserTrendingPodcastsUseCase(max = any()) } returns flowOf(trendingPodcasts)
            every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(followedPodcasts)
            every { getLocalTrendingPodcastsUseCase(max = any()) } returns flowOf(localTrending)
            every { getForeignTrendingPodcastsUseCase(max = any()) } returns flowOf(foreignTrending)
            every { getLiveEpisodesUseCase(max = any()) } returns flowOf(liveEpisodes)
            every { getChannelsUseCase() } returns flowOf(channels)

            val viewModel = createViewModel()

            viewModel.state.test {
                val success = awaitSettled()
                assertEquals(SectionState.Success(playingEpisodes), success.playingEpisodes)
                assertEquals(SectionState.Success(recentPodcasts), success.userRecentPodcasts)
                assertEquals(SectionState.Success(randomEpisodes), success.randomEpisodes)
                assertEquals(SectionState.Success(trendingPodcasts), success.userTrendingPodcasts)
                assertEquals(SectionState.Success(followedPodcasts), success.followedPodcasts)
                assertEquals(SectionState.Success(localTrending), success.localTrendingPodcasts)
                assertEquals(
                    SectionState.Success(foreignTrending),
                    success.foreignTrendingPodcasts,
                )
                assertEquals(SectionState.Success(liveEpisodes), success.liveEpisodes)
                assertEquals(SectionState.Success(channels), success.channels)
            }
        }

    @Test
    fun `Given all flows emit empty, When collecting, Then state is Success with empty lists`() =
        runTest {
            setupDefaultMocks()

            val viewModel = createViewModel()

            viewModel.state.test {
                val success = awaitSettled()
                assertEquals(SectionState.Success(emptyList<Episode>()), success.playingEpisodes)
                assertEquals(SectionState.Success(emptyList<Podcast>()), success.userRecentPodcasts)
                assertEquals(SectionState.Success(emptyList<Channel>()), success.channels)
            }
        }

    @Test
    fun `Given one flow throws, When collecting, Then only that section is Error`() = runTest {
        // 소스 하나가 실패했다고 홈 전체가 오류 화면이 되면 안 된다. 나머지 여덟 개는
        // 멀쩡히 도착했고, 사용자에게 보여줄 것이 남아 있다.
        setupDefaultMocks()
        // 보여줄 것이 하나는 있어야 한다. 전부 비어 있는데 하나가 실패하면 그때는 화면에
        // 내놓을 것이 없어 오류 화면이 맞고, 그 경우는 아래 별도 테스트가 다룬다.
        every { getChannelsUseCase() } returns flowOf(channelTestDataList)
        every { getPlayingEpisodesUseCase(max = any()) } returns flow {
            throw RuntimeException("Error")
        }

        val viewModel = createViewModel()

        viewModel.state.test {
            val success = awaitSettled()
            val failed = success.playingEpisodes
            assertTrue(failed is SectionState.Error)
            // RuntimeException 은 DataErrorException 이 아니므로 asDataError() 가 Unexpected 로
            // 접어 올린다 — 판별 로직 자체는 core:model 쪽 책임이라 여기서는 그 결과가 섹션에
            // 그대로 실렸는지만 확인한다.
            assertTrue((failed as SectionState.Error).error is DataError.Unexpected)
            // 실패한 것은 한 섹션뿐이고 나머지는 그대로 화면에 오른다.
            assertEquals(SectionState.Success(channelTestDataList), success.channels)
        }
    }

    @Test
    fun `Given remote fails while local is empty, When collecting, Then state is Error`() =
        runTest {
            // 오프라인 첫 실행. 로컬만 읽는 셋(이어듣기·팔로우·채널)은 빈 목록으로 **성공**
            // 하고 원격은 전부 실패한다. "모든 섹션이 실패했는가" 로 판정하면 이 상황이
            // 영영 걸리지 않아, 화면은 아무 설명도 재시도 버튼도 없는 빈 시트가 된다.
            setupDefaultMocks()
            val boom = { flow<Nothing> { throw RuntimeException("offline") } }
            every { getUserRecentPodcastsUseCase(max = any()) } returns boom()
            every { getMyRandomEpisodesUseCase(max = any()) } returns boom()
            every { getUserTrendingPodcastsUseCase(max = any()) } returns boom()
            every { getLocalTrendingPodcastsUseCase(max = any()) } returns boom()
            every { getForeignTrendingPodcastsUseCase(max = any()) } returns boom()
            every { getLiveEpisodesUseCase(max = any()) } returns boom()

            val viewModel = createViewModel()

            viewModel.state.test {
                var state = awaitItem()
                while (state !is HomeState.Error) state = awaitItem()
                assertTrue(state.error is DataError.Unexpected)
            }
        }

    @Test
    fun `Given every flow throws, When collecting, Then state is Error`() = runTest {
        // 보여줄 것이 하나도 없으면 화면 전체가 오류를 다룬다.
        throwFromAllSources()

        val viewModel = createViewModel()

        viewModel.state.test {
            var state = awaitItem()
            while (state !is HomeState.Error) state = awaitItem()
            assertTrue(state.error is DataError.Unexpected)
        }
    }

    @Test
    fun `Given one slow flow, When collecting, Then the others do not wait for it`() = runTest {
        // 이 ViewModel 이 고쳐야 했던 것. 랜덤 에피소드가 아직 응답하지 않아도 나머지는
        // 곧바로 화면에 오른다. 예전에는 값째로 combine 해 가장 느린 하나가 전체를 붙잡았다.
        setupDefaultMocks()
        val slowRandom = MutableSharedFlow<List<Episode>>()
        every { getMyRandomEpisodesUseCase(max = any()) } returns slowRandom
        every { getUserRecentPodcastsUseCase(max = any()) } returns flowOf(podcastTestDataList)

        val viewModel = createViewModel()

        viewModel.state.test {
            var state = awaitItem()
            while (
                state !is HomeState.Success ||
                state.userRecentPodcasts !is SectionState.Success
            ) {
                state = awaitItem()
            }

            // 랜덤만 아직 오지 않았고, 나머지는 이미 화면에 올릴 수 있다.
            assertEquals(SectionState.Loading, state.randomEpisodes)
            assertEquals(SectionState.Success(podcastTestDataList), state.userRecentPodcasts)

            // 뒤늦게 도착해도 바뀌는 것은 그 섹션 하나뿐이다.
            slowRandom.emit(episodeTestDataList)
            val arrived = awaitSettled()
            assertEquals(SectionState.Success(episodeTestDataList), arrived.randomEpisodes)
            assertEquals(SectionState.Success(podcastTestDataList), arrived.userRecentPodcasts)
        }
    }

    @Test
    fun `Given Retry action after failure, When sent, Then flows are resubscribed`() = runTest {
        // 재시도 버튼이 뜨는 것은 전체 실패일 때뿐이므로 그 상황을 만든다.
        throwFromAllSources()

        val viewModel = createViewModel()

        viewModel.state.test {
            var state = awaitItem()
            while (state !is HomeState.Error) state = awaitItem()

            viewModel.sendAction(HomeAction.Retry)

            // 재시도해도 같은 예외가 다시 나므로 State 는 여전히 Error 다. 다만 매번 새
            // RuntimeException 인스턴스라 Throwable 의 기본 equals(참조 비교) 때문에 이전
            // 값과 달라 StateFlow 가 재방출한다 — 그 자체가 flatMapLatest 가 실제로 재구독했다는
            // 신호다. 값만으론 "같은 에러가 다시 온 것"과 "재시도가 아예 안 된 것"을 구분할 수
            // 없으므로, combine 안의 소스 UseCase 호출 횟수로 재구독 여부를 명시적으로 검증한다.
            state = awaitItem()
            while (state !is HomeState.Error) state = awaitItem()
            verify(exactly = 2) { getPlayingEpisodesUseCase(max = any()) }
        }
    }

    @Test
    fun `Given PlayEpisode action, When sent, Then playEpisodeUseCase is invoked`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        val episode = episodeTestData

        viewModel.sendAction(HomeAction.PlayEpisode(episode))

        coVerify { playEpisodeUseCase(episode) }
    }

    @Test
    fun `Given ResumeEpisode action, When sent, Then resumeEpisodeUseCase is invoked`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        val episode = episodeTestData

        viewModel.sendAction(HomeAction.ResumeEpisode(episode))

        coVerify { resumeEpisodeUseCase(episode) }
    }

    @Test
    fun `Given ToggleLikedEpisode action, When sent, Then toggleLikedEpisodeUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val episode = episodeTestData

            viewModel.sendAction(HomeAction.ToggleLikedEpisode(episode))

            coVerify { toggleLikedEpisodeUseCase(episode) }
        }

    @Test
    fun `Given ClickPodcast action, When sent, Then NavigateToPodcast effect is emitted`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(HomeAction.ClickPodcast(42L))
                assertEquals(HomeEffect.NavigateToPodcast(42L), awaitItem())
            }
        }

    @Test
    fun `Given ClickChannel action, When sent, Then NavigateToChannel effect is emitted`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(HomeAction.ClickChannel(1L))
                assertEquals(HomeEffect.NavigateToChannel(1L), awaitItem())
            }
        }

    @Test
    fun `Given ClickMore action, When sent, Then NavigateToMore effect carries the section`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(HomeAction.ClickMore(HomeSection.LiveEpisodes))
                assertEquals(HomeEffect.NavigateToMore(HomeSection.LiveEpisodes), awaitItem())
            }
        }

    @Test
    fun `Given ClickMore for another section, When sent, Then that section is carried`() =
        runTest {
            // 섹션이 그대로 실려 가야 한다. 여기가 어긋나면 더 보기를 눌렀을 때 엉뚱한
            // 목록이 열린다.
            setupDefaultMocks()
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(HomeAction.ClickMore(HomeSection.Channels))
                assertEquals(HomeEffect.NavigateToMore(HomeSection.Channels), awaitItem())
            }
        }

    /** 모든 소스가 같은 예외로 실패하게 둔다. 전체 실패(= 화면 전체가 오류)를 만드는 유일한 길이다. */
    private fun throwFromAllSources() {
        val boom = { flow<Nothing> { throw RuntimeException("Error") } }

        every { getPlayingEpisodesUseCase(max = any()) } returns boom()
        every { getUserRecentPodcastsUseCase(max = any()) } returns boom()
        every { getMyRandomEpisodesUseCase(max = any()) } returns boom()
        every { getUserTrendingPodcastsUseCase(max = any()) } returns boom()
        every { getFollowedPodcastsUseCase(max = any()) } returns boom()
        every { getLocalTrendingPodcastsUseCase(max = any()) } returns boom()
        every { getForeignTrendingPodcastsUseCase(max = any()) } returns boom()
        every { getLiveEpisodesUseCase(max = any()) } returns boom()
        every { getChannelsUseCase() } returns boom()
    }

    /**
     * 모든 섹션이 판정될 때까지 흘려보내고 그 상태를 돌려준다.
     *
     * 소스마다 Loading 을 먼저 흘리고 그다음 결과를 내므로 combine 이 중간 상태를 여러 번
     * 낸다. 첫 값 하나만 붙잡으면 아직 다 도착하지 않은 순간을 검사해 테스트가 들쭉날쭉해진다.
     */
    private suspend fun TurbineTestContext<HomeState>.awaitSettled(): HomeState.Success {
        while (true) {
            val state = awaitItem()
            if (state is HomeState.Success && state.sections.none { it is SectionState.Loading }) {
                return state
            }
        }
    }
}
