package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface FeedRepository {
    suspend fun getRecentFeeds(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category>? = null,
        excludeCategories: List<Category>? = null,
    ): List<RecentFeed>

    suspend fun getRecentNewFeeds(
        max: Int? = null,
        since: Instant? = null,
    ): List<RecentNewFeed>

    suspend fun getRecentNewValueFeeds(
        max: Int? = null,
        since: Instant? = null,
    ): List<RecentNewValueFeed>

    suspend fun getRecentSoundbites(max: Int? = null): List<Soundbite>
}