package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPodcastByFeedUrlUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    operator fun invoke(feedUrl: String): Flow<Podcast?> {
        return podcastRepository.getPodcastByFeedUrl(feedUrl)
    }
}
