package io.jacob.episodive.core.domain.usecase.image

import io.jacob.episodive.core.domain.repository.ImageRepository
import javax.inject.Inject

class GetDominantColorFromUrlUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
) {
    suspend operator fun invoke(url: String): ULong {
        return imageRepository.extractDominantColorFromUrl(url)
    }
}