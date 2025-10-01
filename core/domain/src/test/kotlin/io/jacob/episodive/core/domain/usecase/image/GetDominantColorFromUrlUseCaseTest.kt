package io.jacob.episodive.core.domain.usecase.image

import io.jacob.episodive.core.domain.repository.ImageRepository
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GetDominantColorFromUrlUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val imageRepository = mockk<ImageRepository>(relaxed = true)

    private val useCase = GetDominantColorFromUrlUseCase(
        imageRepository = imageRepository,
    )

    @After
    fun teardown() {
        confirmVerified(imageRepository)
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
        runTest {
            // Given
            val url = "https://example.com/image.jpg"
            coEvery { imageRepository.extractDominantColorFromUrl(any()) } returns 0xFF000000uL

            // When
            val result = useCase(url)

            // Then
            assertEquals(0xFF000000uL, result)
            coVerifySequence {
                imageRepository.extractDominantColorFromUrl(url)
            }
        }
}