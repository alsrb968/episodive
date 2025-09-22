package io.jacob.episodive.core.data.util.query

import kotlin.time.Duration

interface CacheableQuery {
    val key: String
    val timeToLive: Duration
}