package io.jacob.episodive.core.domain.usecase.podcast

import androidx.paging.PagingData
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrendingPodcastsPagingUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
) {
    operator fun invoke(
        max: Int,
        language: String? = null,
        categories: List<Category> = emptyList(),
    ): Flow<PagingData<Podcast>> {
        return podcastRepository.getTrendingPodcastsPaging(max, language, categories)
    }
}
