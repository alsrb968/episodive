package io.jacob.episodive.core.database.model

import androidx.room.Embedded
import kotlin.time.Instant

data class LikedEpisodeDto(
    @Embedded val episode: EpisodeEntity?,
    val likedAt: Instant,
)
