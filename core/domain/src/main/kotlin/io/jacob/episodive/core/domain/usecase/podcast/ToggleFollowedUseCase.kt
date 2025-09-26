package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class ToggleFollowedUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    suspend operator fun invoke(id: Long): Boolean {
        val podcast = podcastRepository.getPodcastByFeedId(id).first()
        Timber.i("podcast: $podcast")
        return podcastRepository.toggleFollowed(id)
    }
}