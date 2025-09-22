package io.jacob.episodive.core.model

import kotlin.time.Instant

data class FollowedPodcast(
    val podcast: Podcast,
    val followedAt: Instant,
    val isNotificationEnabled: Boolean,
)
