package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.network.mapper.toCommaString
import io.jacob.episodive.core.network.mapper.toLong
import io.jacob.episodive.core.network.mapper.toRecentFeeds
import io.jacob.episodive.core.network.mapper.toRecentNewFeeds
import io.jacob.episodive.core.network.mapper.toRecentNewValueFeeds
import io.jacob.episodive.core.network.mapper.toSoundbites
import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.network.NetworkDataSource
import javax.inject.Inject
import kotlin.time.Instant

class FeedRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource
) : FeedRepository {
    override suspend fun getRecentFeeds(
        max: Int?,
        since: Instant?,
        language: String?,
        includeCategories: List<Category>?,
        excludeCategories: List<Category>?,
    ): List<RecentFeed> {
        return networkDataSource.getRecentFeeds(
            max = max,
            since = since?.toLong(),
            language = language,
            includeCategories = includeCategories?.toCommaString(),
            excludeCategories = excludeCategories?.toCommaString()
        ).toRecentFeeds()
    }

    override suspend fun getRecentNewFeeds(
        max: Int?,
        since: Instant?,
    ): List<RecentNewFeed> {
        return networkDataSource.getRecentNewFeeds(
            max = max,
            since = since?.toLong(),
        ).toRecentNewFeeds()
    }

    override suspend fun getRecentNewValueFeeds(
        max: Int?,
        since: Instant?,
    ): List<RecentNewValueFeed> {
        return networkDataSource.getRecentNewValueFeeds(
            max = max,
            since = since?.toLong(),
        ).toRecentNewValueFeeds()
    }

    override suspend fun getRecentSoundbites(max: Int?): List<Soundbite> {
        return networkDataSource.getRecentSoundbites(max = max).toSoundbites()
    }
}