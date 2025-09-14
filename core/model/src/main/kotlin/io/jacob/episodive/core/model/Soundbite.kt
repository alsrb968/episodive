package io.jacob.episodive.core.model

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Soundbite(
    val enclosureUrl: String? = null,
    val title: String,
    val startTime: Instant,
    val duration: Duration,
    val episodeId: Long? = null,
    val episodeTitle: String? = null,
    val feedTitle: String? = null,
    val feedUrl: String? = null,
    val feedId: Long? = null,
)
