package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.FollowedPodcast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFollowedPodcastsUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    operator fun invoke(): Flow<List<FollowedPodcast>> {
        return podcastRepository.getFollowedPodcasts()
    }
}