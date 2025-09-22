package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.data.util.query.FeedQuery
import io.jacob.episodive.core.data.util.cache.isTrendingFeedsExpired
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toTrendingFeedEntities
import io.jacob.episodive.core.database.model.TrendingFeedEntity
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toTrendingFeeds
import io.jacob.episodive.core.network.model.TrendingFeedResponse

class TrendingFeedRemoteUpdater(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    override val query: FeedQuery,
) : BaseRemoteUpdater<TrendingFeedEntity, FeedQuery, TrendingFeedResponse>(query) {

    override suspend fun fetchFromNetwork(query: FeedQuery): List<TrendingFeedResponse> {
        return when (query) {
            is FeedQuery.Trending ->
                remoteDataSource.getTrendingFeeds(
                    language = query.language,
                    includeCategories = query.categories.toCommaString(),
                )

            else -> emptyList()
        }
    }

    override suspend fun mapToEntities(
        responses: List<TrendingFeedResponse>,
        cacheKey: String
    ): List<TrendingFeedEntity> {
        return responses.toTrendingFeeds().toTrendingFeedEntities(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<TrendingFeedEntity>) {
        localDataSource.upsertTrendingFeeds(entities)
    }

    override suspend fun isExpired(cached: List<TrendingFeedEntity>): Boolean {
        return cached.isTrendingFeedsExpired(query.timeToLive)
    }
}