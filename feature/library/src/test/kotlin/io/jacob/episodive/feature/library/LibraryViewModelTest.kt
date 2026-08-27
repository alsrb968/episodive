package io.jacob.episodive.feature.library

import app.cash.turbine.test
import io.jacob.episodive.core.domain.usecase.FindInLibraryUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetAllPlayedEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetAllPlayedEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLikedEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLikedEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetSavedEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetSavedEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.SaveEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.opml.ExportOpmlUseCase
import io.jacob.episodive.core.domain.usecase.opml.ImportOpmlUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.ResumeEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsOnceUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.ToggleFollowedUseCase
import io.jacob.episodive.core.domain.usecase.user.GetPreferredCategoriesUseCase
import io.jacob.episodive.core.domain.usecase.user.GetSelectableCategoriesUseCase
import io.jacob.episodive.core.domain.usecase.user.ToggleCategoryUseCase
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.LibraryFindResult
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.model.opml.OpmlImportProgress
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlin.time.Instant
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val findInLibraryUseCase = mockk<FindInLibraryUseCase>(relaxed = true)
    private val getAllPlayedEpisodesUseCase = mockk<GetAllPlayedEpisodesUseCase>(relaxed = true)
    private val getLikedEpisodesUseCase = mockk<GetLikedEpisodesUseCase>(relaxed = true)
    private val getFollowedPodcastsUseCase = mockk<GetFollowedPodcastsUseCase>(relaxed = true)
    private val getPreferredCategoriesUseCase =
        mockk<GetPreferredCategoriesUseCase>(relaxed = true)
    private val getSelectableCategoriesUseCase =
        mockk<GetSelectableCategoriesUseCase>(relaxed = true)
    private val getAllPlayedEpisodesPagingUseCase =
        mockk<GetAllPlayedEpisodesPagingUseCase>(relaxed = true)
    private val getLikedEpisodesPagingUseCase =
        mockk<GetLikedEpisodesPagingUseCase>(relaxed = true)
    private val getFollowedPodcastsPagingUseCase =
        mockk<GetFollowedPodcastsPagingUseCase>(relaxed = true)
    private val playEpisodeUseCase = mockk<PlayEpisodeUseCase>(relaxed = true)
    private val resumeEpisodeUseCase = mockk<ResumeEpisodeUseCase>(relaxed = true)
    private val toggleLikedEpisodeUseCase = mockk<ToggleLikedEpisodeUseCase>(relaxed = true)
    private val toggleFollowedUseCase = mockk<ToggleFollowedUseCase>(relaxed = true)
    private val toggleCategoryUseCase = mockk<ToggleCategoryUseCase>(relaxed = true)
    private val getSavedEpisodesUseCase = mockk<GetSavedEpisodesUseCase>(relaxed = true)
    private val getSavedEpisodesPagingUseCase = mockk<GetSavedEpisodesPagingUseCase>(relaxed = true)
    private val saveEpisodeUseCase = mockk<SaveEpisodeUseCase>(relaxed = true)
    private val getFollowedPodcastsOnceUseCase = mockk<GetFollowedPodcastsOnceUseCase>(relaxed = true)
    private val exportOpmlUseCase = mockk<ExportOpmlUseCase>(relaxed = true)
    private val importOpmlUseCase = mockk<ImportOpmlUseCase>(relaxed = true)

    private fun setupDefaultMocks() {
        every { findInLibraryUseCase(any()) } returns flowOf(LibraryFindResult())
        every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getLikedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getPreferredCategoriesUseCase() } returns flowOf(emptyList())
        every { getSelectableCategoriesUseCase() } returns flowOf(emptyList())
        every { getSavedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
    }

    private fun createViewModel(): LibraryViewModel {
        return LibraryViewModel(
            findInLibraryUseCase = findInLibraryUseCase,
            getAllPlayedEpisodesUseCase = getAllPlayedEpisodesUseCase,
            getLikedEpisodesUseCase = getLikedEpisodesUseCase,
            getFollowedPodcastsUseCase = getFollowedPodcastsUseCase,
            getPreferredCategoriesUseCase = getPreferredCategoriesUseCase,
            getSelectableCategoriesUseCase = getSelectableCategoriesUseCase,
            getAllPlayedEpisodesPagingUseCase = getAllPlayedEpisodesPagingUseCase,
            getLikedEpisodesPagingUseCase = getLikedEpisodesPagingUseCase,
            getFollowedPodcastsPagingUseCase = getFollowedPodcastsPagingUseCase,
            playEpisodeUseCase = playEpisodeUseCase,
            resumeEpisodeUseCase = resumeEpisodeUseCase,
            toggleLikedEpisodeUseCase = toggleLikedEpisodeUseCase,
            toggleFollowedUseCase = toggleFollowedUseCase,
            toggleCategoryUseCase = toggleCategoryUseCase,
            getSavedEpisodesUseCase = getSavedEpisodesUseCase,
            getSavedEpisodesPagingUseCase = getSavedEpisodesPagingUseCase,
            saveEpisodeUseCase = saveEpisodeUseCase,
            getFollowedPodcastsOnceUseCase = getFollowedPodcastsOnceUseCase,
            exportOpmlUseCase = exportOpmlUseCase,
            importOpmlUseCase = importOpmlUseCase,
        )
    }

    @After
    fun teardown() {
        confirmVerified(
            playEpisodeUseCase,
            resumeEpisodeUseCase,
            toggleLikedEpisodeUseCase,
            toggleFollowedUseCase,
            toggleCategoryUseCase,
            saveEpisodeUseCase,
            getFollowedPodcastsOnceUseCase,
            exportOpmlUseCase,
            importOpmlUseCase,
        )
    }

    @Test
    fun `Given no emissions, When ViewModel is created, Then initial state is Loading`() = runTest {
        every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf()
        every { getLikedEpisodesUseCase(max = any()) } returns flowOf()
        every { getSavedEpisodesUseCase(max = any()) } returns flowOf()
        every { getFollowedPodcastsUseCase(max = any()) } returns flowOf()
        every { getPreferredCategoriesUseCase() } returns flowOf()
        every { getSelectableCategoriesUseCase() } returns flowOf()

        val viewModel = createViewModel()

        assertEquals(LibraryState.Loading, viewModel.state.value)
    }

    @Test
    fun `Given all flows emit with empty query, When collecting, Then state is Success`() =
        runTest {
            val playedEpisodes = episodeTestDataList.take(3)
            val likedEpisodes = episodeTestDataList.take(2)
            val followedPodcasts = podcastTestDataList.take(2)
            val preferredCategories = listOf(Category.BUSINESS)
            val selectableCategories = listOf(
                SelectableCategory(Category.BUSINESS, true),
                SelectableCategory(Category.COMEDY, false),
            )

            every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf(playedEpisodes)
            every { getLikedEpisodesUseCase(max = any()) } returns flowOf(likedEpisodes)
            every { getSavedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(followedPodcasts)
            every { getPreferredCategoriesUseCase() } returns flowOf(preferredCategories)
            every { getSelectableCategoriesUseCase() } returns flowOf(selectableCategories)

            val viewModel = createViewModel()

            viewModel.state.test {
                assertEquals(LibraryState.Loading, awaitItem())
                // Advance past debounce(500L) on _findResult after subscription starts upstream
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
                val state = awaitItem()
                assertTrue(state is LibraryState.Success)
                val success = state as LibraryState.Success
                assertEquals("", success.findQuery)
                assertEquals(playedEpisodes, success.allPlayedEpisodes)
                assertEquals(likedEpisodes, success.likedEpisodes)
                assertEquals(followedPodcasts, success.followedPodcasts)
                assertEquals(preferredCategories, success.preferredCategories)
                assertEquals(selectableCategories, success.selectableCategories)
                assertEquals(LibrarySection.All, success.section)
            }
        }

    @Test
    fun `Given flow throws, When collecting, Then state is Error with Unexpected DataError`() = runTest {
        val thrown = RuntimeException("Error")
        every { getAllPlayedEpisodesUseCase(max = any()) } returns kotlinx.coroutines.flow.flow {
            throw thrown
        }
        every { getLikedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getSavedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
        every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(emptyList())
        every { getPreferredCategoriesUseCase() } returns flowOf(emptyList())
        every { getSelectableCategoriesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is LibraryState.Error)
            // RuntimeException 은 DataErrorException 이 아니므로 Unexpected 로 떨어져야 한다.
            // 참조 동일성은 비교하지 않는다 — 코루틴의 스택트레이스 복구가 코루틴 경계를 넘을 때
            // 예외를 복제해 새 인스턴스를 만들기 때문에 메시지·타입만 확인한다.
            val error = (state as LibraryState.Error).error
            assertTrue(error is DataError.Unexpected)
            assertEquals(thrown.message, (error as DataError.Unexpected).throwable?.message)
        }
    }

    @Test
    fun `Given error state, When Retry action is sent, Then sources are resubscribed and state recovers`() =
        runTest {
            every { getAllPlayedEpisodesUseCase(max = any()) } returns kotlinx.coroutines.flow.flow {
                throw RuntimeException("Error")
            }
            every { getLikedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getSavedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(emptyList())
            every { getPreferredCategoriesUseCase() } returns flowOf(emptyList())
            every { getSelectableCategoriesUseCase() } returns flowOf(emptyList())

            val viewModel = createViewModel()

            viewModel.state.test {
                val errorState = awaitItem()
                assertTrue(errorState is LibraryState.Error)

                // 재시도 이후에는 정상 데이터가 오도록 스텁을 교체한다.
                every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf(emptyList())

                viewModel.sendAction(LibraryAction.Retry)
                // _findResult 경로가 재구독되며 debounce(500L) 타이머가 다시 걸린다.
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)

                val state = awaitItem()
                assertTrue(state is LibraryState.Success)
            }

            // 최초 구독 + Retry 로 인한 재구독, 총 2회 호출돼야 재시도가 실제로 소스를
            // 다시 구독한다는 증거가 된다. 1회면 재시도가 캐시된 실패를 그대로 반환한 것.
            verify(exactly = 2) { getAllPlayedEpisodesUseCase(max = any()) }
        }

    @Test
    fun `Given ClickFind action, When sent, Then findQuery updates`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.ClickFind("query"))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            // Advance past debounce(500L) on _findResult
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals("query", (state as LibraryState.Success).findQuery)
        }
    }

    @Test
    fun `Given ClearQuery action, When sent, Then findQuery becomes empty`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.ClickFind("query"))
        viewModel.sendAction(LibraryAction.ClearQuery)

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            // Advance past debounce(500L) on _findResult
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals("", (state as LibraryState.Success).findQuery)
        }
    }

    @Test
    fun `Given ClickPlayingEpisode with isCompleted true, When sent, Then playEpisodeUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val episode = episodeTestData.copy(isCompleted = true)

            viewModel.sendAction(LibraryAction.ClickPlayingEpisode(episode))

            coVerify { playEpisodeUseCase(episode) }
        }

    @Test
    fun `Given ClickPlayingEpisode with isCompleted false, When sent, Then resumeEpisodeUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val episode = episodeTestData.copy(isCompleted = false)

            viewModel.sendAction(LibraryAction.ClickPlayingEpisode(episode))

            coVerify { resumeEpisodeUseCase(episode) }
        }

    @Test
    fun `Given ClickEpisode action, When sent, Then playEpisodeUseCase is invoked`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()
        val episode = episodeTestData

        viewModel.sendAction(LibraryAction.ClickEpisode(episode))

        coVerify { playEpisodeUseCase(episode) }
    }

    @Test
    fun `Given ClickPodcast action, When sent, Then NavigateToPodcast effect is emitted`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val podcast = podcastTestData

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.ClickPodcast(podcast))
                assertEquals(LibraryEffect.NavigateToPodcast(podcast.id), awaitItem())
            }
        }

    @Test
    fun `Given ToggleLikedEpisode action, When sent, Then toggleLikedEpisodeUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val episode = episodeTestData

            viewModel.sendAction(LibraryAction.ToggleLikedEpisode(episode))

            coVerify { toggleLikedEpisodeUseCase(episode) }
        }

    @Test
    fun `Given ToggleFollowedPodcast action, When sent, Then toggleFollowedUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val podcast = podcastTestData

            viewModel.sendAction(LibraryAction.ToggleFollowedPodcast(podcast))

            coVerify { toggleFollowedUseCase(podcast.id) }
        }

    @Test
    fun `Given TogglePreferredCategory action, When sent, Then toggleCategoryUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()

            viewModel.sendAction(LibraryAction.TogglePreferredCategory(Category.BUSINESS))

            coVerify { toggleCategoryUseCase(Category.BUSINESS) }
        }

    @Test
    fun `Given QueryChanged action, When sent, Then findQuery updates after debounce`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.QueryChanged("search"))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals("search", (state as LibraryState.Success).findQuery)
        }
    }

    @Test
    fun `Given non-empty find result with query, When collecting, Then state uses find result data`() =
        runTest {
            val findResult = LibraryFindResult(
                playingEpisodes = episodeTestDataList.take(1),
                likedEpisodes = episodeTestDataList.drop(1).take(1),
                followedPodcasts = podcastTestDataList.take(1),
            )
            every { findInLibraryUseCase(any()) } returns flowOf(findResult)
            every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf(episodeTestDataList.take(3))
            every { getLikedEpisodesUseCase(max = any()) } returns flowOf(episodeTestDataList.take(2))
            every { getSavedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(podcastTestDataList.take(2))
            every { getPreferredCategoriesUseCase() } returns flowOf(listOf(Category.BUSINESS))
            every { getSelectableCategoriesUseCase() } returns flowOf(emptyList())

            val viewModel = createViewModel()
            viewModel.sendAction(LibraryAction.ClickFind("query"))

            viewModel.state.test {
                assertEquals(LibraryState.Loading, awaitItem())
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
                val state = awaitItem()
                assertTrue(state is LibraryState.Success)
                val success = state as LibraryState.Success
                assertEquals(findResult.playingEpisodes, success.allPlayedEpisodes)
                assertEquals(findResult.likedEpisodes, success.likedEpisodes)
                assertEquals(findResult.followedPodcasts, success.followedPodcasts)
                assertEquals(emptyList<Category>(), success.preferredCategories)
            }
        }

    @Test
    fun `Given all flows emit empty data, When collecting, Then state is Success with empty collections`() =
        runTest {
            setupDefaultMocks()

            val viewModel = createViewModel()

            viewModel.state.test {
                assertEquals(LibraryState.Loading, awaitItem())
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
                val state = awaitItem()
                assertTrue(state is LibraryState.Success)
                val success = state as LibraryState.Success
                assertEquals(emptyList<Any>(), success.allPlayedEpisodes)
                assertEquals(emptyList<Any>(), success.likedEpisodes)
                assertEquals(emptyList<Any>(), success.followedPodcasts)
                assertEquals(emptyList<Any>(), success.preferredCategories)
                assertEquals(emptyList<Any>(), success.selectableCategories)
            }
        }

    @Test
    fun `Given SelectSection action, When sent, Then section updates in state`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.SelectSection(LibrarySection.Liked))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            // Advance past debounce(500L) on _findResult
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals(LibrarySection.Liked, (state as LibraryState.Success).section)
        }
    }

    @Test
    fun `Given SelectSection Followed, When sent, Then section updates to Followed`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.SelectSection(LibrarySection.Followed))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals(LibrarySection.Followed, (state as LibraryState.Success).section)
        }
    }

    @Test
    fun `Given SelectSection RecentlyListened, When sent, Then section updates to RecentlyListened`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.SelectSection(LibrarySection.RecentlyListened))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals(LibrarySection.RecentlyListened, (state as LibraryState.Success).section)
        }
    }

    @Test
    fun `Given SelectSection Preferred, When sent, Then section updates to Preferred`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.SelectSection(LibrarySection.Preferred))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals(LibrarySection.Preferred, (state as LibraryState.Success).section)
        }
    }

    @Test
    fun `Given ToggleSavedEpisode action, When sent, Then saveEpisodeUseCase is invoked`() =
        runTest {
            setupDefaultMocks()
            val viewModel = createViewModel()
            val episode = episodeTestData

            viewModel.sendAction(LibraryAction.ToggleSavedEpisode(episode))

            coVerify { saveEpisodeUseCase(episode) }
        }

    @Test
    fun `Given saved episodes exist, When collecting, Then state includes saved episodes`() =
        runTest {
            val savedEpisodes = episodeTestDataList.take(2)
            every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getLikedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getSavedEpisodesUseCase(max = any()) } returns flowOf(savedEpisodes)
            every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(emptyList())
            every { getPreferredCategoriesUseCase() } returns flowOf(emptyList())
            every { getSelectableCategoriesUseCase() } returns flowOf(emptyList())

            val viewModel = createViewModel()

            viewModel.state.test {
                assertEquals(LibraryState.Loading, awaitItem())
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
                val state = awaitItem()
                assertTrue(state is LibraryState.Success)
                assertEquals(savedEpisodes, (state as LibraryState.Success).savedEpisodes)
            }
        }

    @Test
    fun `Given SelectSection Saved, When sent, Then section updates to Saved`() = runTest {
        setupDefaultMocks()
        val viewModel = createViewModel()

        viewModel.sendAction(LibraryAction.SelectSection(LibrarySection.Saved))

        viewModel.state.test {
            assertEquals(LibraryState.Loading, awaitItem())
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
            val state = awaitItem()
            assertTrue(state is LibraryState.Success)
            assertEquals(LibrarySection.Saved, (state as LibraryState.Success).section)
        }
    }

    @Test
    fun `Given find query with results, When collecting, Then state uses find result data`() =
        runTest {
            val findResult = LibraryFindResult(
                playingEpisodes = episodeTestDataList.take(1),
                likedEpisodes = episodeTestDataList.take(1),
                savedEpisodes = emptyList(),
                followedPodcasts = podcastTestDataList.take(1),
            )
            every { findInLibraryUseCase(any()) } returns flowOf(findResult)
            every { getAllPlayedEpisodesUseCase(max = any()) } returns flowOf(episodeTestDataList)
            every { getLikedEpisodesUseCase(max = any()) } returns flowOf(episodeTestDataList)
            every { getSavedEpisodesUseCase(max = any()) } returns flowOf(emptyList())
            every { getFollowedPodcastsUseCase(max = any()) } returns flowOf(podcastTestDataList)
            every { getPreferredCategoriesUseCase() } returns flowOf(listOf(Category.BUSINESS))
            every { getSelectableCategoriesUseCase() } returns flowOf(emptyList())

            val viewModel = createViewModel()

            viewModel.sendAction(LibraryAction.ClickFind("query"))

            viewModel.state.test {
                assertEquals(LibraryState.Loading, awaitItem())
                mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(600)
                val state = awaitItem()
                assertTrue(state is LibraryState.Success)
                val success = state as LibraryState.Success
                assertEquals("query", success.findQuery)
                assertEquals(findResult.playingEpisodes, success.allPlayedEpisodes)
                assertEquals(findResult.likedEpisodes, success.likedEpisodes)
                assertEquals(findResult.followedPodcasts, success.followedPodcasts)
            }
        }

    // --- OPML 내보내기 ---

    @Test
    fun `Given ExportOpmlUseCase succeeds, When ExportOpml sent, Then ShowOpmlExported effect carries count`() =
        runTest {
            setupDefaultMocks()
            coEvery { exportOpmlUseCase(any()) } returns 5
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.ExportOpml("content://dest"))
                assertEquals(LibraryEffect.ShowOpmlExported(5), awaitItem())
            }

            coVerify { exportOpmlUseCase("content://dest") }
        }

    @Test
    fun `Given no followed podcasts, When ExportOpml sent, Then ShowOpmlEmpty effect is emitted`() =
        runTest {
            setupDefaultMocks()
            // UseCase 계약: 팔로우가 하나도 없으면 파일을 쓰지 않고 0을 돌려준다.
            coEvery { exportOpmlUseCase(any()) } returns 0
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.ExportOpml("content://dest"))
                assertEquals(LibraryEffect.ShowOpmlEmpty, awaitItem())
            }

            coVerify { exportOpmlUseCase("content://dest") }
        }

    @Test
    fun `Given ExportOpmlUseCase throws, When ExportOpml sent, Then ShowOpmlExportFailed effect is emitted`() =
        runTest {
            setupDefaultMocks()
            coEvery { exportOpmlUseCase(any()) } throws RuntimeException("disk full")
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.ExportOpml("content://dest"))
                assertEquals(LibraryEffect.ShowOpmlExportFailed, awaitItem())
            }

            coVerify { exportOpmlUseCase("content://dest") }
        }

    // --- OPML 가져오기 ---

    @Test
    fun `Given followed podcasts exist, When RequestOpmlExport, Then file picker is asked to open`() =
        runTest {
            setupDefaultMocks()
            coEvery { getFollowedPodcastsOnceUseCase() } returns podcastTestDataList
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.RequestOpmlExport)
                assertEquals(LibraryEffect.LaunchOpmlExport, awaitItem())
            }

            coVerify { getFollowedPodcastsOnceUseCase() }
        }

    @Test
    fun `Given no followed podcasts, When RequestOpmlExport, Then picker never opens`() =
        runTest {
            // SAF 의 CreateDocument 는 URI 를 주기 전에 이미 파일을 만든다. 선택기를 먼저
            // 열고 나서 "내보낼 것이 없다" 를 알리면, 알림과 별개로 사용자의 다운로드 폴더에
            // 0바이트짜리 .opml 이 남는다. 실기기에서 실제로 그렇게 남는 것을 확인하고
            // 이 사전 확인 단계를 넣었다 — 이 테스트가 그것을 지킨다.
            setupDefaultMocks()
            coEvery { getFollowedPodcastsOnceUseCase() } returns emptyList()
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.RequestOpmlExport)
                assertEquals(LibraryEffect.ShowOpmlEmpty, awaitItem())
                expectNoEvents()
            }

            coVerify { getFollowedPodcastsOnceUseCase() }
            coVerify(exactly = 0) { exportOpmlUseCase(any()) }
        }

    @Test
    fun `Given ImportOpml action, When progress emits, Then opmlProgress fills without anyone subscribing state`() =
        runTest {
            setupDefaultMocks()
            val step1 = OpmlImportProgress(total = 2, done = 1, added = 1)
            val step2 = OpmlImportProgress(total = 2, done = 2, added = 2, isFinished = true)
            every { importOpmlUseCase(any()) } returns flowOf(step1, step2)
            // 일부러 viewModel.state 를 한 번도 구독하지 않는다 — state 는
            // stateIn(WhileSubscribed(5_000)) 라, 진행률이 그 체인에 얹혀 있었다면
            // 구독자가 없는 이 테스트에서는 애초에 어떤 값도 못 받았을 것이다.
            val viewModel = createViewModel()

            viewModel.opmlProgress.test {
                assertEquals(null, awaitItem())
                viewModel.sendAction(LibraryAction.ImportOpml("content://source"))

                // 중간값을 하나씩 못박지 않는다. opmlProgress 는 StateFlow 라 같은 틱에
                // 연달아 방출되면 앞의 값이 접힌다 — 화면도 최신 상태만 그리면 되므로
                // 그것이 정상 동작이고, 여기서 지키려는 계약은 "state 를 아무도 구독하지
                // 않아도 진행률이 끝까지 채워진다" 이다.
                var latest = awaitItem()
                while (latest?.isFinished != true) {
                    latest = awaitItem()
                }
                assertEquals(step2, latest)
            }

            coVerify { importOpmlUseCase("content://source") }
        }

    @Test
    fun `Given import finishes, When collecting effect, Then no completion effect is emitted`() =
        runTest {
            setupDefaultMocks()
            val finished = OpmlImportProgress(total = 1, done = 1, added = 1, isFinished = true)
            every { importOpmlUseCase(any()) } returns flowOf(finished)
            val viewModel = createViewModel()

            // 완료는 opmlProgress.isFinished 로만 알 수 있다 — _effect 는 replay 0 이라
            // 탭을 옮긴 사이 끝나면 완료 통지가 증발하므로 애초에 여기서 내보내지 않는다.
            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.ImportOpml("content://source"))
                expectNoEvents()
            }
            assertEquals(true, viewModel.opmlProgress.value?.isFinished)

            coVerify { importOpmlUseCase("content://source") }
        }

    @Test
    fun `Given import already running, When ImportOpml sent again, Then importOpmlUseCase runs once`() =
        runTest {
            setupDefaultMocks()
            // 채널로 흐름을 직접 통제해 첫 import 가 "진행 중" 인 순간을 확정한다.
            // progress 값이 아니라 Job 의 생존 여부로 중복을 막는지 확인하려면, 첫 emit
            // 이후에도 job 이 살아있는 상태를 인위적으로 유지해야 한다.
            val channel = Channel<OpmlImportProgress>(Channel.UNLIMITED)
            every { importOpmlUseCase(any()) } returns channel.receiveAsFlow()

            val viewModel = createViewModel()

            viewModel.sendAction(LibraryAction.ImportOpml("content://first"))
            // 두 번째 요청 — 첫 import 의 Job 이 아직 채널을 기다리며 살아있는 상태다.
            viewModel.sendAction(LibraryAction.ImportOpml("content://second"))

            channel.close()

            coVerify(exactly = 1) { importOpmlUseCase(any()) }
        }

    @Test
    fun `Given ImportOpmlUseCase flow throws, When collecting, Then ShowOpmlImportFailed effect emitted and opmlProgress cleared`() =
        runTest {
            setupDefaultMocks()
            every { importOpmlUseCase(any()) } returns flow { throw RuntimeException("파일을 읽을 수 없다") }
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(LibraryAction.ImportOpml("content://broken"))
                assertEquals(LibraryEffect.ShowOpmlImportFailed, awaitItem())
            }
            assertEquals(null, viewModel.opmlProgress.value)

            coVerify { importOpmlUseCase("content://broken") }
        }

    @Test
    fun `Given progress is unfinished, When DismissOpmlProgress sent, Then progress is kept`() =
        runTest {
            setupDefaultMocks()
            val channel = Channel<OpmlImportProgress>(Channel.UNLIMITED)
            every { importOpmlUseCase(any()) } returns channel.receiveAsFlow()
            val inProgress = OpmlImportProgress(total = 3, done = 1, added = 1)
            channel.trySend(inProgress)

            val viewModel = createViewModel()
            viewModel.sendAction(LibraryAction.ImportOpml("content://source"))
            assertEquals(inProgress, viewModel.opmlProgress.value)

            // 진행 중에 지우면 시트가 닫혔다가 다음 progress 로 곧 다시 채워지므로,
            // 진행 중에는 지우지 않는 것이 계약이다.
            viewModel.sendAction(LibraryAction.DismissOpmlProgress)
            assertEquals(inProgress, viewModel.opmlProgress.value)

            channel.close()

            coVerify { importOpmlUseCase("content://source") }
        }

    @Test
    fun `Given progress is finished, When DismissOpmlProgress sent, Then progress is cleared`() =
        runTest {
            setupDefaultMocks()
            val finished = OpmlImportProgress(total = 1, done = 1, added = 1, isFinished = true)
            every { importOpmlUseCase(any()) } returns flowOf(finished)
            val viewModel = createViewModel()

            viewModel.sendAction(LibraryAction.ImportOpml("content://source"))
            assertEquals(finished, viewModel.opmlProgress.value)

            viewModel.sendAction(LibraryAction.DismissOpmlProgress)
            assertEquals(null, viewModel.opmlProgress.value)

            coVerify { importOpmlUseCase("content://source") }
        }
}
