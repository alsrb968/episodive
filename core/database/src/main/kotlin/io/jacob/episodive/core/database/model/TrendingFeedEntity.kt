package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.Index
import io.jacob.episodive.core.model.Category
import kotlin.time.Instant

@Entity(
    tableName = "trending_feeds",
    primaryKeys = ["id", "cacheKey"],
    indices = [Index(value = ["id", "cachedAt"])]
)
data class TrendingFeedEntity(
    val id: Long,
    val url: String,
    val title: String,
    val description: String,
    val author: String,
    val image: String,
    val artwork: String,
    val newestItemPublishTime: Instant,
    val itunesId: Long? = null,
    val trendScore: Int,
    val language: String,
    val categories: List<Category> = emptyList(),
    val cacheKey: String,
    val cachedAt: Instant,
)
