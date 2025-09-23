package io.jacob.episodive.core.domain.usecase.feed

import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
) {
    operator fun invoke(
        language: String? = null,
        categories: List<Category> = emptyList()
    ): Flow<List<RecentFeed>> {
        return feedRepository.getRecentFeeds(
            language = language,
            includeCategories = categories
        )
    }
}