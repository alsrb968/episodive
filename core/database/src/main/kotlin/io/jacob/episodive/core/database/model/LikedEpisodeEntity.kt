package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "liked_episodes")
data class LikedEpisodeEntity(
    @PrimaryKey val id: Long,
    val likedAt: Instant,
)
