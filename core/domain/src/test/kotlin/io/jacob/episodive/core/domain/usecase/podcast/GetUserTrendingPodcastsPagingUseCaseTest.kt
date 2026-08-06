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

class GetUserTrendingPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val getTrendingPodcastsPagingUseCase =
        mockk<GetTrendingPodcastsPagingUseCase>(relaxed = true)

    private val useCase = GetUserTrendingPodcastsPagingUseCase(
        userRepository = userRepository,
        getTrendingPodcastsPagingUseCase = getTrendingPodcastsPagingUseCase,
    )

    @After
    fun teardown() {
        confirmVerified(userRepository, getTrendingPodcastsPagingUseCase)
    }

    @Test
    fun `Given not empty categories, when invoke called, then trending paging called`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(
                    language = "ko",
                    categories = listOf(Category.AFTER_SHOWS, Category.BUSINESS)
                )
            )
            coEvery {
                getTrendingPodcastsPagingUseCase(any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase(50).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                getTrendingPodcastsPagingUseCase(
                    50,
                    "ko",
                    listOf(Category.AFTER_SHOWS, Category.BUSINESS),
                )
            }
        }

    @Test
    fun `Given empty categories, when invoke called, then emits empty paging data`() =
        runTest {
            // 관심 카테고리가 없으면 조회할 조건 자체가 없다. 원격을 치지 않고 빈 목록을 낸다.
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
