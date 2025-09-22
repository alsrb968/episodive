package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(
    tableName = "soundbites",
    primaryKeys = ["episodeId", "cacheKey"],
    indices = [Index(value = ["episodeId", "cachedAt"])]
)
data class SoundbiteEntity(
    val enclosureUrl: String,
    val title: String,
    val startTime: Instant,
    val duration: Duration,
    val episodeId: Long,
    val episodeTitle: String,
    val feedTitle: String,
    val feedUrl: String,
    val feedId: Long,
    val cacheKey: String,
    val cachedAt: Instant,
)
