package io.jacob.episodive.core.model

import kotlin.time.Instant

data class LikedEpisode(
    val episode: Episode,
    val likedAt: Instant,
)
