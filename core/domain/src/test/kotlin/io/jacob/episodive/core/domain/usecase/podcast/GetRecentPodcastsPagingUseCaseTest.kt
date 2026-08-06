package io.jacob.episodive.core.domain.usecase.podcast

import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class GetRecentPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastRepository = mockk<PodcastRepository>(relaxed = true)

    private val useCase = GetRecentPodcastsPagingUseCase(
        podcastRepository = podcastRepository,
    )

    @After
    fun teardown() {
        confirmVerified(podcastRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then paging repository called`() =
        runTest {
            // Given & When
            useCase(50).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                podcastRepository.getRecentPodcastsPaging(50)
            }
        }
}
