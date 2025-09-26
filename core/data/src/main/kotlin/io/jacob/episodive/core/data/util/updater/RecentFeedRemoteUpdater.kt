package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.data.util.query.FeedQuery
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toRecentFeedEntities
import io.jacob.episodive.core.database.mapper.toRecentFeedEntity
import io.jacob.episodive.core.database.model.RecentFeedEntity
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toRecentFeed
import io.jacob.episodive.core.network.mapper.toRecentFeeds
import io.jacob.episodive.core.network.model.RecentFeedResponse
import kotlin.time.Clock

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

    override suspend fun fetchFromNetworkSingle(query: FeedQuery): RecentFeedResponse? {
        return null
    }

    override suspend fun mapToEntities(
        responses: List<RecentFeedResponse>,
        cacheKey: String
    ): List<RecentFeedEntity> {
        return responses.toRecentFeeds().toRecentFeedEntities(cacheKey)
    }

    override suspend fun mapToEntity(
        response: RecentFeedResponse?,
        cacheKey: String
    ): RecentFeedEntity? {
        return response?.toRecentFeed()?.toRecentFeedEntity(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<RecentFeedEntity>) {
        localDataSource.upsertRecentFeeds(entities)
    }

    override suspend fun saveToLocal(entity: RecentFeedEntity?) {
        entity?.let { localDataSource.upsertRecentFeeds(listOf(it)) }
    }

    override suspend fun isExpired(cached: List<RecentFeedEntity>): Boolean {
        if (cached.isEmpty()) return true
        val oldestCache = cached.minByOrNull { it.cachedAt }?.cachedAt
            ?: return true
        val now = Clock.System.now()
        return now - oldestCache > query.timeToLive
    }

    override suspend fun isExpired(cached: RecentFeedEntity?): Boolean {
        if (cached == null) return true
        val oldCached = cached.cachedAt
        val now = Clock.System.now()
        return now - oldCached > query.timeToLive
    }
}