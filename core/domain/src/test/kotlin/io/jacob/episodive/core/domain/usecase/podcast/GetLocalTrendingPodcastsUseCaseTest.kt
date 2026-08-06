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
import java.util.Locale

class GetLocalTrendingPodcastsUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val getTrendingPodcastsUseCase = mockk<GetTrendingPodcastsUseCase>(relaxed = true)

    private val useCase = GetLocalTrendingPodcastsUseCase(
        userRepository = userRepository,
        getTrendingPodcastsUseCase = getTrendingPodcastsUseCase,
    )

    @After
    fun teardown() {
        confirmVerified(
            userRepository,
            getTrendingPodcastsUseCase,
        )
    }

    @Test
    fun `Given empty categories, When invoke called, Then still queries by language`() =
        runTest {
            // 관심 카테고리가 없어도 언어만으로 조회한다. 이 섹션은 사용자 설정을 기다릴
            // 이유가 없다.
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(UserData())
            coEvery {
                getTrendingPodcastsUseCase(any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase(10).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                getTrendingPodcastsUseCase(10, Locale.getDefault().language, emptyList())
            }
        }

    @Test
    fun `Given not empty categories, When invoke called, Then categories are not applied`() =
        runTest {
            // 카테고리를 걸면 GetUserTrendingPodcastsUseCase 와 같은 조건이 되어 홈에 같은
            // 목록이 두 번 뜬다. 이 유스케이스의 존재 이유가 카테고리를 걸지 않는 것이다.
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(
                    categories = listOf(Category.AFTER_SHOWS, Category.BUSINESS)
                )
            )
            coEvery {
                getTrendingPodcastsUseCase(any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase(10).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                getTrendingPodcastsUseCase(10, Locale.getDefault().language, emptyList())
            }
        }
}
