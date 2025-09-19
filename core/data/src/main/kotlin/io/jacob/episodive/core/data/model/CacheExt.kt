package io.jacob.episodive.core.data.model

import io.jacob.episodive.core.database.model.EpisodeEntity
import kotlin.collections.minByOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun EpisodeEntity.isExpired(timeToLive: Duration = 24.hours): Boolean {
    val now = Clock.System.now()
    return now - cachedAt > timeToLive
}

fun List<EpisodeEntity>.isExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}