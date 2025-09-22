package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Instant

@Entity(
    tableName = "recent_new_feeds",
    primaryKeys = ["id", "cacheKey"],
    indices = [Index(value = ["id", "cachedAt"])]
)
data class RecentNewFeedEntity(
    val id: Long,
    val url: String,
    val timeAdded: Instant,
    val status: String,
    val contentHash: String,
    val language: String,
    val cacheKey: String,
    val cachedAt: Instant,
)