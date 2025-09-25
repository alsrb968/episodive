package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.PodcastRepository
import javax.inject.Inject

class AddFollowedsUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    suspend operator fun invoke(ids: List<Long>): Boolean {
        return podcastRepository.addFolloweds(ids)
    }
}