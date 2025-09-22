package io.jacob.episodive.core.data.util

import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toRecentNewFeedEntities
import io.jacob.episodive.core.database.model.RecentNewFeedEntity
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toRecentNewFeeds
import io.jacob.episodive.core.network.model.RecentNewFeedResponse

class RecentNewFeedRemoteUpdater(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    override val query: FeedQuery,
) : BaseRemoteUpdater<RecentNewFeedEntity, FeedQuery, RecentNewFeedResponse>(query) {

    override suspend fun fetchFromNetwork(query: FeedQuery): List<RecentNewFeedResponse> {
        return when (query) {
            is FeedQuery.RecentNew -> remoteDataSource.getRecentNewFeeds()
            else -> emptyList()
        }
    }

    override suspend fun mapToEntities(
        responses: List<RecentNewFeedResponse>,
        cacheKey: String
    ): List<RecentNewFeedEntity> {
        return responses.toRecentNewFeeds().toRecentNewFeedEntities(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<RecentNewFeedEntity>) {
        localDataSource.upsertRecentNewFeeds(entities)
    }

    override suspend fun isExpired(cached: List<RecentNewFeedEntity>): Boolean {
        return cached.isRecentNewFeedsExpired(query.timeToLive)
    }
}