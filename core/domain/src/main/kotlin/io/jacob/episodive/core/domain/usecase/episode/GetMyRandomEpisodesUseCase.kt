package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 홈의 랜덤 에피소드.
 *
 * **관심 카테고리는 넘기지 않는다. 언어와 함께 보내면 원격이 급격히 느려진다.**
 * `episodes/random` 을 조합별로 재보면(2026-08, max=6, 각 3~5회):
 *
 * | 파라미터 | 응답 |
 * |:--|:--|
 * | `max` 만 | 0.54 ~ 0.71초 |
 * | `max+lang` | 1.0 ~ 1.4초 |
 * | `max+cat` | 0.54 ~ 0.72초 |
 * | `max+lang+cat` | **2.7 ~ 27초** |
 *
 * 카테고리를 하나로 줄여도(`cat=1`) 2.3~16초라 개수 문제가 아니다. 두 필터의 교집합에서
 * 표본을 찾느라 헤매는 것으로 보인다. 같은 시각 다른 엔드포인트(`podcasts/trending`,
 * `recent/feeds`, `episodes/live`)는 모두 0.5~0.9초였으니 서버 전체가 느린 것도 아니다.
 *
 * 이 섹션은 홈에서 위로부터 둘째 자리라 그 지연이 첫 화면의 절반을 스켈레톤으로 덮었다.
 * 그래서 언어만 맞추고 카테고리는 포기한다 — 랜덤은 원래 관심사 밖의 것을 만나는 자리라
 * 잃는 것이 가장 적다.
 *
 * 클라이언트에서 거르는 우회는 막혔다. `max=40&lang=ko` 는 그 자체로 1.8~3.3초인데다
 * 받아 본 40개의 카테고리 분포가 Religion·Music·Business 쪽이라, 사용자가 고른 카테고리로
 * 거르면 섹션이 통째로 비는 일이 잦다.
 *
 * 원격이 고쳐지면 되돌릴 수 있게 [EpisodeRepository.getRandomEpisodes] 의
 * `includeCategories` 는 남겨 둔다. [GetMyRandomEpisodesPagingUseCase] 도 같은 이유로 같다.
 */
class GetMyRandomEpisodesUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    private val userRepository: UserRepository,
) {
    operator fun invoke(max: Int): Flow<List<Episode>> {
        // UserData 전체가 아니라 language 만 본다. 통째로 flatMapLatest 하면 재생 속도처럼
        // 이 쿼리와 무관한 값이 바뀔 때마다 흐름을 끊고 다시 구독해, 갱신을 다시 돌리고
        // (페이징 쪽은) Pager 를 새로 만들어 **보던 목록이 맨 위로 되감긴다.**
        return userRepository.getUserData()
            .map { it.language }
            .distinctUntilChanged()
            .flatMapLatest { language ->
                episodeRepository.getRandomEpisodes(
                    max = max,
                    language = language,
                )
            }
    }
}