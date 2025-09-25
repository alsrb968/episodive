package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddFollowedsUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastRepository = mockk<PodcastRepository>(relaxed = true)

    private val useCase = AddFollowedsUseCase(
        podcastRepository = podcastRepository,
    )

    @After
    fun teardown() {
        confirmVerified(podcastRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
        runTest {
            // Given
            val ids = listOf(1L, 2L, 3L)
            coEvery { podcastRepository.addFolloweds(any()) } returns true

            // When
            val result = useCase(ids)

            // Then
            assertTrue(result)
            coVerifySequence {
                podcastRepository.addFolloweds(ids)
            }
        }
}