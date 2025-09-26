package io.jacob.episodive.core.domain.usecase.user

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class SetFirstLaunchOffUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val useCase = SetFirstLaunchOffUseCase(
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
            coEvery { userRepository.setFirstLaunch(any()) } just Runs

            // When
            useCase()

            // Then
            coVerifySequence {
                userRepository.setFirstLaunch(false)
            }
        }
}