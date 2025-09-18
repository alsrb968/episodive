package io.jacob.episodive.core.database.model

import androidx.room.Embedded
import kotlin.time.Duration
import kotlin.time.Instant

data class PlayedEpisodeDto(
    @Embedded val episode: EpisodeEntity?,
    val playedAt: Instant,
    val position: Duration,
    val isCompleted: Boolean,
)
