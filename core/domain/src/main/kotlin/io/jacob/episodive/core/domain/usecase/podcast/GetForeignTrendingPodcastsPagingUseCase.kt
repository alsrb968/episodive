package io.jacob.episodive.core.domain.usecase.podcast

import androidx.paging.PagingData
import io.jacob.episodive.core.domain.util.EmptyLoadStates
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** [GetForeignTrendingPodcastsUseCase] 의 전체 목록판. */
class GetForeignTrendingPodcastsPagingUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val getTrendingPodcastsPagingUseCase: GetTrendingPodcastsPagingUseCase,
) {
    operator fun invoke(max: Int): Flow<PagingData<Podcast>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            if (userData.categories.isEmpty()) {
                // 무인자 empty() 는 로드 상태를 갱신하지 않아 refresh 가 Loading 에 머문다.
                // 그러면 화면이 '항목 없음' 대신 스켈레톤을 영원히 돌린다.
                flowOf(PagingData.empty(sourceLoadStates = EmptyLoadStates))
            } else {
                val foreignLanguages =
                    languages.filter { it != userData.language }.joinToString(",")
                getTrendingPodcastsPagingUseCase(
                    max = max,
                    language = foreignLanguages,
                    categories = userData.categories,
                )
            }
        }
    }

    companion object {
        private val languages = listOf("en", "es", "fr", "de", "it", "ja", "ko", "pt", "ru", "zh")
    }
}
