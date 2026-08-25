package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Episode
import javax.inject.Inject

/**
 * 로컬에 없는 에피소드를 원격에서 한 건 가져온다. 없거나 실패하면 null.
 *
 * [GetEpisodeByIdUseCase] 는 로컬 DB 만 흘려보내므로, 공유받은 링크로 들어온 낯선 에피소드는
 * 그쪽으로는 영영 잡히지 않는다.
 */
class FetchEpisodeByIdUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
) {
    suspend operator fun invoke(id: Long): Episode? = episodeRepository.fetchEpisodeById(id)
}
