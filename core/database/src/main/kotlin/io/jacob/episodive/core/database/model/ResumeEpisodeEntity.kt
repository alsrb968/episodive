package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(
    tableName = "resume_episodes",
    foreignKeys = [
        ForeignKey(
            entity = EpisodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResumeEpisodeEntity(
    @PrimaryKey val id: Long,
    val lastPlayedAt: Instant,
    val position: Duration,
)
