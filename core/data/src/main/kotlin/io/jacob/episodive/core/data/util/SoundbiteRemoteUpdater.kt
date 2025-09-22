package io.jacob.episodive.core.data.util

import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toSoundbiteEntities
import io.jacob.episodive.core.database.model.SoundbiteEntity
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toSoundbites
import io.jacob.episodive.core.network.model.SoundbiteResponse

class SoundbiteRemoteUpdater(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    override val query: FeedQuery,
) : BaseRemoteUpdater<SoundbiteEntity, FeedQuery, SoundbiteResponse>(query) {

    override suspend fun fetchFromNetwork(query: FeedQuery): List<SoundbiteResponse> {
        return when (query) {
            is FeedQuery.Soundbite -> remoteDataSource.getRecentSoundbites()
            else -> emptyList()
        }
    }

    override suspend fun mapToEntities(
        responses: List<SoundbiteResponse>,
        cacheKey: String
    ): List<SoundbiteEntity> {
        return responses.toSoundbites().toSoundbiteEntities(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<SoundbiteEntity>) {
        localDataSource.upsertSoundbites(entities)
    }

    override suspend fun isExpired(cached: List<SoundbiteEntity>): Boolean {
        return cached.isSoundbitesExpired(query.timeToLive)
    }
}