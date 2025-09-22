package io.jacob.episodive.core.model

import kotlin.time.Duration
import kotlin.time.Instant

data class PlayedEpisode(
    val episode: Episode,
    val playedAt: Instant,
    val position: Duration,
    val isCompleted: Boolean,
)
