package io.jacob.episodive.core.domain.usecase

import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class FindInLibraryUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastRepository = mockk<PodcastRepository>(relaxed = true)
    private val episodeRepository = mockk<EpisodeRepository>(relaxed = true)

    private val useCase = FindInLibraryUseCase(
        podcastRepository = podcastRepository,
        episodeRepository = episodeRepository,
    )

    @After
    fun teardown() {
        confirmVerified(podcastRepository, episodeRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repositories called`() =
        runTest {
            coEvery { episodeRepository.getPlayingEpisodes(any()) } returns mockk(relaxed = true)
            coEvery { episodeRepository.getLikedEpisodes(any()) } returns mockk(relaxed = true)
            coEvery { podcastRepository.getFollowedPodcasts(any()) } returns mockk(relaxed = true)

            // When
            useCase("query").test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                episodeRepository.getPlayingEpisodes("query")
                episodeRepository.getLikedEpisodes("query")
                podcastRepository.getFollowedPodcasts("query")
            }
        }
}