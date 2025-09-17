package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Duration
import kotlin.time.Instant

@Entity("resume_episodes")
data class ResumeEpisodeEntity(
    @PrimaryKey val id: Long,
    val lastPlayedAt: Instant,
    val position: Duration,
)
