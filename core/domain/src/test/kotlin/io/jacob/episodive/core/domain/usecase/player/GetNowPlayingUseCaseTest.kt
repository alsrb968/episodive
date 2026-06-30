package io.jacob.episodive.core.domain.usecase.player

import app.cash.turbine.test
import io.jacob.episodive.core.domain.download.EpisodeDownloader
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class GetNowPlayingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playerRepository = mockk<PlayerRepository>(relaxed = true)
    private val episodeRepository = mockk<EpisodeRepository>(relaxed = true)
    private val episodeDownloader = mockk<EpisodeDownloader>(relaxed = true)

    private val useCase = GetNowPlayingUseCase(
        playerRepository = playerRepository,
        episodeRepository = episodeRepository,
        episodeDownloader = episodeDownloader,
        ioDispatcher = mainDispatcherRule.testDispatcher,
    )

    @After
    fun teardown() {
        confirmVerified(playerRepository, episodeRepository, episodeDownloader)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository and downloader combined`() =
        runTest {
            // Given
            coEvery { playerRepository.nowPlaying } returns flowOf(episodeTestData)
            coEvery { episodeRepository.getEpisodeById(any()) } returns flowOf(episodeTestData)
            every { episodeDownloader.observeActiveDownloads() } returns flowOf(emptyMap())

            // When
            useCase().test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerify {
                playerRepository.nowPlaying
                episodeRepository.getEpisodeById(any())
                episodeDownloader.observeActiveDownloads()
            }
        }
}
