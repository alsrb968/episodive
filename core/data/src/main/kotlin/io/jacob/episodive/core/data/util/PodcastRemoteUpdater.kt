package io.jacob.episodive.core.data.util

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.mapper.toPodcastEntities
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.mapper.toPodcasts

class PodcastRemoteUpdater @AssistedInject constructor(
    private val localDataSource: PodcastLocalDataSource,
    private val remoteDataSource: PodcastRemoteDataSource,
    @Assisted("query") private val query: PodcastQuery,
) : RemoteUpdater<PodcastEntity> {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: PodcastQuery): PodcastRemoteUpdater
    }

    override suspend fun load(cached: List<PodcastEntity>) {
        try {
            if (cached.isPodcastsExpired(query.timeToLive)) {
                val networkResult = when (query) {
                    is PodcastQuery.Search -> remoteDataSource.searchPodcasts(query.query)
                    is PodcastQuery.Medium -> remoteDataSource.getPodcastsByMedium(query.medium)
                }
                val podcasts = networkResult.toPodcasts()

                localDataSource.upsertPodcasts(podcasts.toPodcastEntities(query.key))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}