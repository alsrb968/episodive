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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class GetUserRecentPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val getRecentPodcastsPagingUseCase =
        mockk<GetRecentPodcastsPagingUseCase>(relaxed = true)

    private val useCase = GetUserRecentPodcastsPagingUseCase(
        userRepository = userRepository,
        getRecentPodcastsPagingUseCase = getRecentPodcastsPagingUseCase,
    )

    @After
    fun teardown() {
        confirmVerified(userRepository, getRecentPodcastsPagingUseCase)
    }

    @Test
    fun `Given not empty categories, when invoke called, then recent paging called`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(language = "ko", categories = listOf(Category.BUSINESS))
            )
            coEvery {
                getRecentPodcastsPagingUseCase(any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase(50).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                getRecentPodcastsPagingUseCase(50, "ko", listOf(Category.BUSINESS))
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
