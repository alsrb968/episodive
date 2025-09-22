package io.jacob.episodive.core.database.model

import androidx.room.Embedded
import kotlin.time.Instant

data class FollowedPodcastDto(
    @Embedded val podcast: PodcastEntity?,
    val followedAt: Instant,
    val isNotificationEnabled: Boolean,
)
