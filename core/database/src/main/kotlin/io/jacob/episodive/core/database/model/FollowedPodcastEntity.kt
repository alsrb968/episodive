package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "followed_podcasts")
data class FollowedPodcastEntity(
    @PrimaryKey val id: Long,
    val followedAt: Instant,
    val isNotificationEnabled: Boolean,
)
