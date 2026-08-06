package io.jacob.episodive.core.domain.usecase.podcast

import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.UserData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GetForeignTrendingPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val getTrendingPodcastsPagingUseCase =
        mockk<GetTrendingPodcastsPagingUseCase>(relaxed = true)

    private val useCase = GetForeignTrendingPodcastsPagingUseCase(
        userRepository = userRepository,
        getTrendingPodcastsPagingUseCase = getTrendingPodcastsPagingUseCase,
    )

    @After
    fun teardown() {
        confirmVerified(userRepository, getTrendingPodcastsPagingUseCase)
    }

    @Test
    fun `Given user language, when invoke called, then that language is excluded`() =
        runTest {
            // Given
            val languageSlot = slot<String>()
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(language = "ko", categories = listOf(Category.BUSINESS))
            )
            coEvery {
                getTrendingPodcastsPagingUseCase(any(), capture(languageSlot), any())
            } returns mockk(relaxed = true)

            // When
            useCase(50).test {
                awaitComplete()
            }

            // Then
            val languages = languageSlot.captured.split(",")
            assertFalse(languages.contains("ko"))
            assertTrue(languages.contains("en"))

            coVerifySequence {
                userRepository.getUserData()
                getTrendingPodcastsPagingUseCase(50, any(), listOf(Category.BUSINESS))
            }
        }

    @Test
    fun `Given empty categories, when invoke called, then emits empty paging data`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(UserData(language = "ko"))

            // When
            useCase(50).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
            }
        }
}
