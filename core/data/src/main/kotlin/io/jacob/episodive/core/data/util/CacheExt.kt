package io.jacob.episodive.core.data.util

import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.PodcastEntity
import kotlin.collections.minByOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun EpisodeEntity.isEpisodeExpired(timeToLive: Duration = 24.hours): Boolean {
    val now = Clock.System.now()
    return now - cachedAt > timeToLive
}

fun List<EpisodeEntity>.isEpisodesExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}

fun PodcastEntity.isPodcastExpired(timeToLive: Duration = 24.hours): Boolean {
    val now = Clock.System.now()
    return now - cachedAt > timeToLive
}

fun List<PodcastEntity>.isPodcastsExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}