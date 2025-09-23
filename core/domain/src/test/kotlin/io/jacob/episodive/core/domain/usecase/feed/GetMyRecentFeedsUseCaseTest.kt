package io.jacob.episodive.core.domain.usecase.feed

import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.FeedRepository
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

class GetMyRecentFeedsUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val feedRepository = mockk<FeedRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val useCase = GetMyRecentFeedsUseCase(
        feedRepository = feedRepository,
        userRepository = userRepository,
    )

    @After
    fun teardown() {
        confirmVerified(feedRepository, userRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
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
                feedRepository.getRecentFeeds(any(), any(), any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase().test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                feedRepository.getRecentFeeds(any(), any(), any(), any(), any())
            }
        }
}