package io.jacob.episodive.core.domain.usecase.player

import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 지금 **소리가 나고 있는** 에피소드의 id. 일시정지 중이거나 아무것도 안 올라가 있으면 null 이다.
 *
 * 목록에서 "재생 중" 배지를 붙이는 용도라 [GetNowPlayingUseCase] 처럼 에피소드 전체를 싣지 않는다.
 * 그쪽은 Room 조회와 다운로드 상태까지 묶어 오므로 배지 하나에 쓰기엔 무겁다.
 *
 * `nowPlaying` 과 `isPlaying` 을 combine 하지만, 이는 CLAUDE.md 의 재생 위치 저장 규약이 금지하는
 * 조합과 다르다. 그 규약은 **position 을 저장할 때** 늦게 도착하는 `nowPlaying` 의 id 를 쓰지 말라는
 * 것이고, 여기서는 position 을 다루지 않는다. 전환 순간 한 프레임 어긋나도 배지가 잠깐 늦게 붙을 뿐
 * 저장되는 데이터가 없다.
 */
class GetPlayingEpisodeIdUseCase @Inject constructor(
    @param:Player(EpisodivePlayers.Main) private val playerRepository: PlayerRepository,
) {
    operator fun invoke(): Flow<Long?> =
        combine(
            playerRepository.nowPlaying,
            playerRepository.isPlaying,
        ) { episode, isPlaying ->
            episode?.id?.takeIf { isPlaying }
        }.distinctUntilChanged()
}
