package io.jacob.episodive.core.model

import kotlin.time.Instant

data class Trending(
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
    val categories: List<Category>? = null,
)
