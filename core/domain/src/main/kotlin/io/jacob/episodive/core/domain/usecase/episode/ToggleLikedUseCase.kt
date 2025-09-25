package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import javax.inject.Inject

class ToggleLikedUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
) {
    suspend operator fun invoke(id: Long): Boolean {
        return episodeRepository.toggleLiked(id)
    }
}