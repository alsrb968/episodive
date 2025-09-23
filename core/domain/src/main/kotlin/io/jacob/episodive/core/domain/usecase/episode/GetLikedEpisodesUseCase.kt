package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.LikedEpisode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLikedEpisodesUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
) {
    operator fun invoke(): Flow<List<LikedEpisode>> {
        return episodeRepository.getLikedEpisodes()
    }
}