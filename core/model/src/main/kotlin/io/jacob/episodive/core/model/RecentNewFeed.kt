package io.jacob.episodive.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class RecentNewFeed(
    val id: Long,
    val url: String,
    val timeAdded: Instant,
    val status: String,
    val contentHash: String,
    val language: String,
)
