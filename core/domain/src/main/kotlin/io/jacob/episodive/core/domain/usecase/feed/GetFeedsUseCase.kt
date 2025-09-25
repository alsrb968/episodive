package io.jacob.episodive.core.domain.usecase.feed

import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Feed
import io.jacob.episodive.core.model.mapper.toFeedsFromRecent
import io.jacob.episodive.core.model.mapper.toFeedsFromTrending
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
) {
    operator fun invoke(
        language: String? = null,
        categories: List<Category> = emptyList()
    ): Flow<List<Feed>> {
        return combine(
            feedRepository.getTrendingFeeds(
                language = language,
                includeCategories = categories
            ),
            feedRepository.getRecentFeeds(
                language = language,
                includeCategories = categories
            ),
        ) { trending, recent ->
            (trending.toFeedsFromTrending() + recent.toFeedsFromRecent())
                .distinctBy { it.id }
                .sortedByDescending { it.newestItemPublishTime }
        }
    }
}