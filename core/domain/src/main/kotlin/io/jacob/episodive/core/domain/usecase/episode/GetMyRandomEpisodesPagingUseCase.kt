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
 * 원본과 마찬가지로 카테고리가 비어 있어도 그대로 요청하고, **원본과 마찬가지로 관심
 * 카테고리를 넘기지 않는다** — 언어와 함께 보내면 원격이 급격히 느려진다. 측정값과 판단
 * 근거는 [GetMyRandomEpisodesUseCase] 에 적어 두었다. 둘 중 하나만 넘기면 같은 화면의
 * 목록과 더 보기가 서로 다른 모수에서 뽑혀, 더 보기를 눌렀을 때 방금 본 항목이 사라진다.
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
            )
        }
    }
}
