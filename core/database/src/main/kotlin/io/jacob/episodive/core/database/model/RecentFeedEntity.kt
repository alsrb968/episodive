package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.Index
import io.jacob.episodive.core.model.Category
import kotlin.time.Instant

@Entity(
    tableName = "recent_feeds",
    primaryKeys = ["id", "cacheKey"],
    indices = [Index(value = ["id", "cachedAt"])]
)
data class RecentFeedEntity(
    val id: Long,
    val url: String,
    val title: String,
    val newestItemPublishTime: Instant,
    val oldestItemPublishTime: Instant? = null,
    val description: String? = null,
    val author: String? = null,
    val image: String? = null,
    val itunesId: Long? = null,
    val trendScore: Int? = null,
    val language: String,
    val categories: List<Category> = emptyList(),
    val cacheKey: String,
    val cachedAt: Instant,
)
