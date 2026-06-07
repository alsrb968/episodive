package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Episode
import javax.inject.Inject

class SyncNewEpisodesUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
    private val episodeRepository: EpisodeRepository,
) {
    suspend operator fun invoke(): List<NewEpisodeResult> {
        val syncTargets = podcastRepository.getFollowedPodcastsToSync()
        return syncTargets.mapNotNull { (feedId, followedAt) ->
            try {
                val since = episodeRepository.getLatestEpisodeDatePublished(feedId) ?: followedAt
                val newEpisodes = episodeRepository.fetchAndSaveNewEpisodes(feedId, since)
                if (newEpisodes.isNotEmpty()) NewEpisodeResult(feedId, newEpisodes) else null
            } catch (_: Exception) {
                null
            }
        }
    }
}

data class NewEpisodeResult(val feedId: Long, val episodes: List<Episode>)
