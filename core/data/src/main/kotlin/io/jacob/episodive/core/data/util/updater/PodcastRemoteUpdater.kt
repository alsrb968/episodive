package io.jacob.episodive.core.data.util.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.data.util.query.PodcastQuery
import io.jacob.episodive.core.data.util.cache.isPodcastsExpired
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.mapper.toPodcastEntities
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.mapper.toPodcasts
import io.jacob.episodive.core.network.model.PodcastResponse

class PodcastRemoteUpdater @AssistedInject constructor(
    private val localDataSource: PodcastLocalDataSource,
    private val remoteDataSource: PodcastRemoteDataSource,
    @Assisted("query") override val query: PodcastQuery,
) : BaseRemoteUpdater<PodcastEntity, PodcastQuery, PodcastResponse>(query) {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: PodcastQuery): PodcastRemoteUpdater
    }

    override suspend fun fetchFromNetwork(query: PodcastQuery): List<PodcastResponse> {
        return when (query) {
            is PodcastQuery.Search -> remoteDataSource.searchPodcasts(query.query)
            is PodcastQuery.Medium -> remoteDataSource.getPodcastsByMedium(query.medium)
        }
    }

    override suspend fun mapToEntities(
        responses: List<PodcastResponse>,
        cacheKey: String
    ): List<PodcastEntity> {
        return responses.toPodcasts().toPodcastEntities(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<PodcastEntity>) {
        localDataSource.upsertPodcasts(entities)
    }

    override suspend fun isExpired(cached: List<PodcastEntity>): Boolean {
        return cached.isPodcastsExpired(query.timeToLive)
    }
}