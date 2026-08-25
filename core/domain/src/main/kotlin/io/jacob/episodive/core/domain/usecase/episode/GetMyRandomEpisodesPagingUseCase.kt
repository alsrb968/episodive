package io.jacob.episodive.core.domain.usecase.episode

import androidx.paging.PagingData
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
        // UserData 전체가 아니라 language 만 본다. 통째로 flatMapLatest 하면 재생 속도처럼
        // 이 쿼리와 무관한 값이 바뀔 때마다 흐름을 끊고 다시 구독해, 갱신을 다시 돌리고
        // (페이징 쪽은) Pager 를 새로 만들어 **보던 목록이 맨 위로 되감긴다.**
        return userRepository.getUserData()
            .map { it.language }
            .distinctUntilChanged()
            .flatMapLatest { language ->
                episodeRepository.getRandomEpisodesPaging(
                    max = max,
                    language = language,
                )
            }
    }
}
