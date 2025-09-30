package io.jacob.episodive.core.domain.usecase.player

import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.model.Episode
import javax.inject.Inject

class PlayEpisodeUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(episode: Episode) {
        playerRepository.play(episode)
    }
}