package io.jacob.episodive.core.network.datasource

import io.jacob.episodive.core.network.model.*

interface FeedRemoteDataSource {
    suspend fun getTrendingFeeds(
        max: Int? = null,
        since: Long? = null,
        language: String? = null,
        includeCategories: String? = null,
        excludeCategories: String? = null,
    ): List<TrendingFeedResponse>

    suspend fun getRecentFeeds(
        max: Int? = null,
        since: Long? = null,
        language: String? = null,
        includeCategories: String? = null,
        excludeCategories: String? = null,
    ): List<RecentFeedResponse>

    suspend fun getRecentNewFeeds(
        max: Int? = null,
        since: Long? = null,
    ): List<RecentNewFeedResponse>

    suspend fun getRecentNewValueFeeds(
        max: Int? = null,
        since: Long? = null,
    ): List<RecentNewValueFeedResponse>

    suspend fun getRecentSoundbites(max: Int? = null): List<SoundbiteResponse>
}