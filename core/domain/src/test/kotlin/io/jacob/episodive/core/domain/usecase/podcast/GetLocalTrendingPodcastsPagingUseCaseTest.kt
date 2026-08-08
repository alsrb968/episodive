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

class GetLocalTrendingPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val getTrendingPodcastsPagingUseCase =
        mockk<GetTrendingPodcastsPagingUseCase>(relaxed = true)

    private val useCase = GetLocalTrendingPodcastsPagingUseCase(
        userRepository = userRepository,
        getTrendingPodcastsPagingUseCase = getTrendingPodcastsPagingUseCase,
    )

    @After
    fun teardown() {
        confirmVerified(userRepository, getTrendingPodcastsPagingUseCase)
    }

    @Test
    fun `Given not empty categories, when invoke called, then categories are not applied`() =
        runTest {
            // 비페이징 쪽과 같은 계약이다 — 카테고리를 걸면 '내 트렌딩' 과 같은 목록이 된다.
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(language = "ko", categories = listOf(Category.BUSINESS))
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
                getTrendingPodcastsPagingUseCase(50, "ko", emptyList())
            }
        }

    @Test
    fun `Given empty categories, when invoke called, then still queries by language`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(UserData(language = "ko"))
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
                getTrendingPodcastsPagingUseCase(50, "ko", emptyList())
            }
        }
}
