package io.jacob.episodive.core.domain.usecase.feed

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Feed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject

class GetRecommendedFeedsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val getFeedsUseCase: GetFeedsUseCase,
) {
    operator fun invoke(): Flow<List<Feed>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            Timber.d("userData: $userData")
            if (userData.categories.isEmpty()) {
                Timber.d("empty")
                flowOf(emptyList())
            } else {
                Timber.d("not empty")
                getFeedsUseCase(
                    language = userData.language,
                    categories = userData.categories,
                )
            }
        }
    }
}