package io.jacob.episodive.core.domain.usecase.feed

import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.RecentFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetMyRecentFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<List<RecentFeed>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            feedRepository.getRecentFeeds(
                language = userData.language,
                includeCategories = userData.categories
            )
        }
    }
}