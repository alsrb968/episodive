package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Progress
import javax.inject.Inject

class UpdatePlayedEpisodeUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository
) {
    suspend operator fun invoke(episodeId: Long, progress: Progress) {
        episodeRepository.updatePlayed(
            id = episodeId,
            position = progress.position,
            isCompleted = progress.positionRatio >= 1f,
        )
    }
}