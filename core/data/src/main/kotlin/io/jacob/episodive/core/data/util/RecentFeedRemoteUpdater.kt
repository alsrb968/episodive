package io.jacob.episodive.core.data.util

import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toRecentFeedEntities
import io.jacob.episodive.core.database.model.RecentFeedEntity
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toRecentFeeds
import io.jacob.episodive.core.network.model.RecentFeedResponse

class RecentFeedRemoteUpdater(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    override val query: FeedQuery,
) : BaseRemoteUpdater<RecentFeedEntity, FeedQuery, RecentFeedResponse>(query) {

    override suspend fun fetchFromNetwork(query: FeedQuery): List<RecentFeedResponse> {
        return when (query) {
            is FeedQuery.Recent ->
                remoteDataSource.getRecentFeeds(
                    language = query.language,
                    includeCategories = query.categories.toCommaString(),
                )

            else -> emptyList()
        }
    }

    override suspend fun mapToEntities(
        responses: List<RecentFeedResponse>,
        cacheKey: String
    ): List<RecentFeedEntity> {
        return responses.toRecentFeeds().toRecentFeedEntities(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<RecentFeedEntity>) {
        localDataSource.upsertRecentFeeds(entities)
    }

    override suspend fun isExpired(cached: List<RecentFeedEntity>): Boolean {
        return cached.isRecentFeedsExpired(query.timeToLive)
    }
}