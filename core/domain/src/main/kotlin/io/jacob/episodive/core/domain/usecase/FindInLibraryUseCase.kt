package io.jacob.episodive.core.domain.usecase

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.LibraryFindResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class FindInLibraryUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
    private val episodeRepository: EpisodeRepository,
) {
    operator fun invoke(query: String): Flow<LibraryFindResult> {
        return combine(
            episodeRepository.getPlayingEpisodes(query),
            episodeRepository.getLikedEpisodes(query),
            podcastRepository.getFollowedPodcasts(query),
        ) { playingEpisodes, likedEpisodes, followedPodcasts ->
            LibraryFindResult(
                playingEpisodes = playingEpisodes,
                likedEpisodes = likedEpisodes,
                followedPodcasts = followedPodcasts,
            )
        }
    }
}