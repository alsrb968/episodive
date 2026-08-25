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

class GetMyRandomEpisodesUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodeRepository = mockk<EpisodeRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val useCase = GetMyRandomEpisodesUseCase(
        episodeRepository = episodeRepository,
        userRepository = userRepository,
    )

    @After
    fun teardown() {
        confirmVerified(episodeRepository, userRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repositories not called`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(UserData(true, "ko", listOf(Category.CAREERS)))
            coEvery {
                episodeRepository.getRandomEpisodes(any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase(10).test {
                awaitComplete()
            }

            // Then
            // 사용자가 카테고리를 골랐어도 원격에는 넘기지 않는다 — lang 과 cat 을 함께
            // 보내면 응답이 2.7~27초로 뛴다(근거는 UseCase KDoc). any() 로 두면 카테고리를
            // 다시 넘기는 회귀가 이 테스트를 그대로 통과한다.
            coVerifySequence {
                userRepository.getUserData()
                episodeRepository.getRandomEpisodes(
                    max = 10,
                    language = "ko",
                    includeCategories = emptyList(),
                )
            }
        }
}