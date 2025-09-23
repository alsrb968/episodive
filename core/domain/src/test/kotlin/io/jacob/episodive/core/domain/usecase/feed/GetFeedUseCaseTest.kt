package io.jacob.episodive.core.domain.usecase.feed

import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.testing.model.recentFeedTestDataList
import io.jacob.episodive.core.testing.model.trendingFeedTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GetFeedUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val feedRepository = mockk<FeedRepository>(relaxed = true)

    private val useCase = GetFeedsUseCase(
        feedRepository = feedRepository,
    )

    @After
    fun teardown() {
        confirmVerified(feedRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
        runTest {
            // Given
            coEvery {
                feedRepository.getTrendingFeeds(any(), any(), any(), any(), any())
            } returns flowOf(trendingFeedTestDataList.take(2))
            coEvery {
                feedRepository.getRecentFeeds(any(), any(), any(), any(), any())
            } returns flowOf(recentFeedTestDataList.take(2))

            // When
            useCase("ko", listOf(Category.BUSINESS)).test {
                val result = awaitItem()
                // Then
                assertEquals(4, result.size)
                assertEquals(5499546, result[0].id)
                assertEquals(7354020, result[1].id)
                assertEquals(7230762, result[2].id)
                assertEquals(7258341, result[3].id)
                awaitComplete()
            }
            // Then
            coVerifySequence {
                feedRepository.getTrendingFeeds(
                    language = "ko",
                    includeCategories = listOf(Category.BUSINESS)
                )
                feedRepository.getRecentFeeds(
                    language = "ko",
                    includeCategories = listOf(Category.BUSINESS)
                )
            }
        }
}