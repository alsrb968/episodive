package io.jacob.episodive.core.domain.usecase.player

import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifySequence
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class PlayEpisodeUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playerRepository = mockk<PlayerRepository>(relaxed = true)

    private val useCase = PlayEpisodeUseCase(
        playerRepository = playerRepository,
    )

    @After
    fun teardown() {
        confirmVerified(playerRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
        runTest {
            // Given
            val episode = episodeTestData
            every { playerRepository.play(any<Episode>()) } just Runs

            // When
            useCase(episode)

            // Then
            verifySequence {
                playerRepository.play(episode)
            }
        }
}