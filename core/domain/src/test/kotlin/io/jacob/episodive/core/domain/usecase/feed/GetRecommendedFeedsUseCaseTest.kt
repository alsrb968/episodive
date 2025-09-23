package io.jacob.episodive.core.domain.usecase.feed

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

class GetRecommendedFeedsUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val getFeedsUseCase = mockk<GetFeedsUseCase>(relaxed = true)

    private val useCase = GetRecommendedFeedsUseCase(
        userRepository = userRepository,
        getFeedsUseCase = getFeedsUseCase,
    )

    @After
    fun teardown() {
        confirmVerified(userRepository, getFeedsUseCase)
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
                getFeedsUseCase(any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase().test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                getFeedsUseCase(any(), any())
            }
        }
}