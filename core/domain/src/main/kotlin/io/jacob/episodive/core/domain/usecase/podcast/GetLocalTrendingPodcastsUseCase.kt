package io.jacob.episodive.core.domain.usecase.podcast

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * 내 언어권에서 인기 있는 팟캐스트.
 *
 * 관심 카테고리를 걸지 않는 것이 [GetUserTrendingPodcastsUseCase] 와의 유일한 차이이자
 * 존재 이유다. 카테고리까지 걸면 두 유스케이스가 같은 조건이 되어 홈에 같은 목록이 두 번
 * 뜬다. 카테고리가 비어 있어도 결과가 나오므로 사용자 설정을 기다릴 필요도 없다 —
 * 언어만 있으면 되기 때문이다.
 */
class GetLocalTrendingPodcastsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val getTrendingPodcastsUseCase: GetTrendingPodcastsUseCase,
) {
    operator fun invoke(max: Int): Flow<List<Podcast>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            getTrendingPodcastsUseCase(
                max = max,
                language = userData.language,
            )
        }
    }
}