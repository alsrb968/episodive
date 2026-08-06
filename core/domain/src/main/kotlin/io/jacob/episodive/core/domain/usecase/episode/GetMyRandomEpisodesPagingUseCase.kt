package io.jacob.episodive.core.domain.usecase.episode

import androidx.paging.PagingData
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * [GetMyRandomEpisodesUseCase] 의 전체 목록판.
 *
 * 원본과 마찬가지로 카테고리가 비어 있어도 그대로 요청한다 — 랜덤은 관심사가 없어도
 * 보여줄 것이 있는 섹션이다.
 */
class GetMyRandomEpisodesPagingUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    private val userRepository: UserRepository,
) {
    operator fun invoke(max: Int): Flow<PagingData<Episode>> {
        return userRepository.getUserData().flatMapLatest { userData ->
            episodeRepository.getRandomEpisodesPaging(
                max = max,
                language = userData.language,
                includeCategories = userData.categories,
            )
        }
    }
}
