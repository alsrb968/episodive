package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.data.util.Cacher
import io.jacob.episodive.core.data.util.FeedQuery
import io.jacob.episodive.core.data.util.TrendingFeedRemoteUpdater
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.TrendingFeed
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.model.mapper.toSeconds
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toRecentFeeds
import io.jacob.episodive.core.network.mapper.toRecentNewFeeds
import io.jacob.episodive.core.network.mapper.toRecentNewValueFeeds
import io.jacob.episodive.core.network.mapper.toSoundbites
import io.jacob.episodive.core.network.mapper.toTrendingFeeds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Instant

class FeedRepositoryImpl @Inject constructor(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    private val remoteUpdater: TrendingFeedRemoteUpdater.Factory,
) : FeedRepository {
    override fun getTrendingFeeds(
        max: Int?,
        since: Instant?,
        language: String?,
        includeCategories: List<Category>,
        excludeCategories: List<Category>,
    ): Flow<List<TrendingFeed>> {
        val query = FeedQuery.Trending(
            language = language,
            categories = includeCategories
        )

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getTrendingFeedsByCacheKey(query.key)
            }
        ).flow.map { it.toTrendingFeeds() }
    }

    override fun getRecentFeeds(
        max: Int?,
        since: Instant?,
        language: String?,
        includeCategories: List<Category>,
        excludeCategories: List<Category>,
    ): Flow<List<RecentFeed>> {
        return feedRemoteDataSource.getRecentFeeds(
            max = max,
            since = since?.toSeconds(),
            language = language,
            includeCategories = includeCategories.toCommaString(),
            excludeCategories = excludeCategories.toCommaString()
        ).toRecentFeeds()
    }

    override fun getRecentNewFeeds(
        max: Int?,
        since: Instant?,
    ): Flow<List<RecentNewFeed>> {
        return feedRemoteDataSource.getRecentNewFeeds(
            max = max,
            since = since?.toSeconds(),
        ).toRecentNewFeeds()
    }

    override fun getRecentNewValueFeeds(
        max: Int?,
        since: Instant?,
    ): Flow<List<RecentNewValueFeed>> {
        return feedRemoteDataSource.getRecentNewValueFeeds(
            max = max,
            since = since?.toSeconds(),
        ).toRecentNewValueFeeds()
    }

    override fun getRecentSoundbites(max: Int?): Flow<List<Soundbite>> {
        return feedRemoteDataSource.getRecentSoundbites(max = max).toSoundbites()
    }
}