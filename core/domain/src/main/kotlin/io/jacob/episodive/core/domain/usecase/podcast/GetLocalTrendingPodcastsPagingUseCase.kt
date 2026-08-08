package io.jacob.episodive.core.domain.usecase.podcast

import androidx.paging.PagingData
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/** [GetLocalTrendingPodcastsUseCase] 의 전체 목록판. 카테고리를 걸지 않는 것도 그대로다. */
class GetLocalTrendingPodcastsPagingUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val getTrendingPodcastsPagingUseCase: GetTrendingPodcastsPagingUseCase,
) {
    operator fun invoke(max: Int): Flow<PagingData<Podcast>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            getTrendingPodcastsPagingUseCase(
                max = max,
                language = userData.language,
            )
        }
    }
}
