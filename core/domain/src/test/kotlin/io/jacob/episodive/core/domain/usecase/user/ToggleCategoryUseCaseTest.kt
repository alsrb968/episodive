package io.jacob.episodive.core.domain.usecase.user

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ToggleCategoryUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val useCase = ToggleCategoryUseCase(
        userRepository = userRepository,
    )

    @After
    fun teardown() {
        confirmVerified(userRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
        runTest {
            // Given
            val category = Category.TECHNOLOGY
            coEvery { userRepository.toggleCategory(any()) } returns true

            // When
            val result = useCase(category)

            // Then
            assertTrue(result)
            coVerifySequence {
                userRepository.toggleCategory(category)
            }
        }
}