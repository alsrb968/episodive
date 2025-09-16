package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.*
import kotlin.time.Instant

interface FeedRepository {
    suspend fun getTrendingFeeds(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category>? = null,
        excludeCategories: List<Category>? = null,
    ): List<TrendingFeed>

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