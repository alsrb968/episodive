package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.TrendingFeed
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface FeedRepository {
    fun getTrendingFeeds(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
        excludeCategories: List<Category> = emptyList(),
    ): Flow<List<TrendingFeed>>

    fun getRecentFeeds(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
        excludeCategories: List<Category> = emptyList(),
    ): Flow<List<RecentFeed>>

    fun getRecentNewFeeds(
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<RecentNewFeed>>

    fun getRecentNewValueFeeds(
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<RecentNewValueFeed>>

    fun getRecentSoundbites(max: Int? = null): Flow<List<Soundbite>>
}