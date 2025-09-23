package io.jacob.episodive.core.domain.usecase.feed

import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.TrendingFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetMyTrendingFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<List<TrendingFeed>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            feedRepository.getTrendingFeeds(
                language = userData.language,
                includeCategories = userData.categories
            )
        }
    }
}