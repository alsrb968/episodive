package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import javax.inject.Inject

class RefreshEpisodeDescriptionUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
) {
    suspend operator fun invoke(id: Long) {
        episodeRepository.refreshEpisodeDescription(id)
    }
}
