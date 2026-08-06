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

class GetTrendingPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastRepository = mockk<PodcastRepository>(relaxed = true)

    private val useCase = GetTrendingPodcastsPagingUseCase(
        podcastRepository = podcastRepository,
    )

    @After
    fun teardown() {
        confirmVerified(podcastRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then paging repository called`() =
        runTest {
            // 비페이징 쪽(getTrendingPodcasts)이 아니라 페이징 쪽을 불러야 한다. 잘못 부르면
            // 전체 목록이 미리보기 캐시 그룹을 덮어써서 홈 섹션까지 함께 망가진다.
            // Given & When
            useCase(50).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                podcastRepository.getTrendingPodcastsPaging(50)
            }
        }
}
