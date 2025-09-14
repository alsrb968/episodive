package io.jacob.episodive.core.model

import kotlin.time.Instant

data class RecentFeed(
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
    val categories: List<Category>? = null,
)
