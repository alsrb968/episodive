package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Podcast
import javax.inject.Inject

class GetFollowedPodcastsOnceUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    suspend operator fun invoke(): List<Podcast> {
        return podcastRepository.getFollowedPodcastsOnce()
    }
}
