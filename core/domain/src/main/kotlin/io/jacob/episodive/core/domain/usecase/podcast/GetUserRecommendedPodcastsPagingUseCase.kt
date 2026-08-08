package io.jacob.episodive.core.domain.usecase.podcast

import androidx.paging.PagingData
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.domain.util.EmptyLoadStates
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetUserRecommendedPodcastsPagingUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val podcastRepository: PodcastRepository,
) {
    operator fun invoke(max: Int): Flow<PagingData<Podcast>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            if (userData.categories.isEmpty()) {
                // 무인자 empty() 로 두면 로드 상태가 Loading 에 머물러 스켈레톤이 영원히
                // 반짝인다. 온보딩의 '다음' 버튼은 선택 개수를 막지 않으므로(enabled = true)
                // 카테고리를 하나도 안 고르고 이 화면에 닿을 수 있다 — 실제로 닿는 경로다.
                flowOf(PagingData.empty(sourceLoadStates = EmptyLoadStates))
            } else {
                podcastRepository.getRecommendedPodcastsPaging(
                    max = max,
                    language = userData.language,
                    includeCategories = userData.categories
                )
            }
        }
    }
}