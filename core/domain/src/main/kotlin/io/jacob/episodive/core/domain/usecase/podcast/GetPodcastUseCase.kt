package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPodcastUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    operator fun invoke(id: Long): Flow<Podcast?> {
        return podcastRepository.getPodcastByFeedId(id)
    }
}