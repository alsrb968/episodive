package io.jacob.episodive.core.model

import kotlin.time.Duration
import kotlin.time.Instant

data class Soundbite(
    val enclosureUrl: String,
    val title: String,
    val startTime: Instant,
    val duration: Duration,
    val episodeId: Long,
    val episodeTitle: String,
    val feedTitle: String,
    val feedUrl: String,
    val feedId: Long,
)
