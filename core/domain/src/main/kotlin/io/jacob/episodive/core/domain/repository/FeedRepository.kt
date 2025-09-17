package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.TrendingFeed
import kotlin.time.Instant

interface FeedRepository {
    suspend fun getTrendingFeeds(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
        excludeCategories: List<Category> = emptyList(),
    ): List<TrendingFeed>

    suspend fun getRecentFeeds(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
        excludeCategories: List<Category> = emptyList(),
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