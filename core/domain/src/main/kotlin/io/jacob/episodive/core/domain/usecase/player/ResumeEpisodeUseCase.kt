package io.jacob.episodive.core.domain.usecase.player

import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.model.PlayedEpisode
import io.jacob.episodive.core.model.mapper.toLongMillis
import javax.inject.Inject

class ResumeEpisodeUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(playedEpisode: PlayedEpisode) {
        playerRepository.play(playedEpisode.episode)
        playerRepository.seekTo(playedEpisode.position.toLongMillis())
    }
}