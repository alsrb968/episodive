package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(tableName = "played_episodes")
data class PlayedEpisodeEntity(
    @PrimaryKey val id: Long,
    val playedAt: Instant,
    val position: Duration,
    val isCompleted: Boolean = false,
)
