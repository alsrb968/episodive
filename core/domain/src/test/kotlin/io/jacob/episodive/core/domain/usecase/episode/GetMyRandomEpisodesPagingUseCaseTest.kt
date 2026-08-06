package io.jacob.episodive.core.domain.usecase.episode

import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.EpisodeRepository
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

class GetMyRandomEpisodesPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodeRepository = mockk<EpisodeRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val useCase = GetMyRandomEpisodesPagingUseCase(
        episodeRepository = episodeRepository,
        userRepository = userRepository,
    )

    @After
    fun teardown() {
        confirmVerified(episodeRepository, userRepository)
    }

    @Test
    fun `Given user data, when invoke called, then random paging called with user condition`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(language = "ko", categories = listOf(Category.BUSINESS))
            )

            // When
            useCase(100).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                episodeRepository.getRandomEpisodesPaging(
                    max = 100,
                    language = "ko",
                    includeCategories = listOf(Category.BUSINESS),
                )
            }
        }

    @Test
    fun `Given empty categories, when invoke called, then still queries`() =
        runTest {
            // 랜덤은 관심사가 없어도 보여줄 것이 있는 섹션이라 카테고리 가드를 두지 않는다.
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(UserData(language = "ko"))

            // When
            useCase(100).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                episodeRepository.getRandomEpisodesPaging(
                    max = 100,
                    language = "ko",
                    includeCategories = emptyList(),
                )
            }
        }
}
