package io.jacob.episodive.core.data.util

import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.database.model.RecentFeedEntity
import io.jacob.episodive.core.database.model.RecentNewFeedEntity
import io.jacob.episodive.core.database.model.SoundbiteEntity
import io.jacob.episodive.core.database.model.TrendingFeedEntity
import kotlin.collections.minByOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun List<EpisodeEntity>.isEpisodesExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}

fun List<PodcastEntity>.isPodcastsExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}

fun List<TrendingFeedEntity>.isTrendingFeedsExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}

fun List<RecentFeedEntity>.isRecentFeedsExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}

fun List<RecentNewFeedEntity>.isRecentNewFeedsExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}

fun List<SoundbiteEntity>.isSoundbitesExpired(timeToLive: Duration = 24.hours): Boolean {
    if (isEmpty()) return true
    val oldestCache = minByOrNull { it.cachedAt } ?: return true
    val now = Clock.System.now()
    return now - oldestCache.cachedAt > timeToLive
}