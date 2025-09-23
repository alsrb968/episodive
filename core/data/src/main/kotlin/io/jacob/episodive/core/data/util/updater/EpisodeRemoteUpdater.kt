package io.jacob.episodive.core.data.util.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.data.util.query.EpisodeQuery
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.mapper.toEpisodes
import io.jacob.episodive.core.network.model.EpisodeResponse
import kotlin.time.Clock

class EpisodeRemoteUpdater @AssistedInject constructor(
    private val localDataSource: EpisodeLocalDataSource,
    private val remoteDataSource: EpisodeRemoteDataSource,
    @Assisted("query") override val query: EpisodeQuery,
) : BaseRemoteUpdater<EpisodeEntity, EpisodeQuery, EpisodeResponse>(query) {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: EpisodeQuery): EpisodeRemoteUpdater
    }

    override suspend fun fetchFromNetwork(query: EpisodeQuery): List<EpisodeResponse> {
        return when (query) {
            is EpisodeQuery.Person -> remoteDataSource.searchEpisodesByPerson(query.person)
            is EpisodeQuery.FeedId -> remoteDataSource.getEpisodesByFeedId(query.feedId)
            is EpisodeQuery.FeedUrl -> remoteDataSource.getEpisodesByFeedUrl(query.feedUrl)
            is EpisodeQuery.PodcastGuid -> remoteDataSource.getEpisodesByPodcastGuid(query.podcastGuid)
            is EpisodeQuery.Live -> remoteDataSource.getLiveEpisodes()
            is EpisodeQuery.Recent -> remoteDataSource.getRecentEpisodes()
        }
    }

    override suspend fun mapToEntities(
        responses: List<EpisodeResponse>,
        cacheKey: String
    ): List<EpisodeEntity> {
        return responses.toEpisodes().toEpisodeEntities(cacheKey)
    }

    override suspend fun saveToLocal(entities: List<EpisodeEntity>) {
        localDataSource.upsertEpisodes(entities)
    }

    override suspend fun isExpired(cached: List<EpisodeEntity>): Boolean {
        if (cached.isEmpty()) return true
        val oldestCache = cached.minByOrNull { it.cachedAt }?.cachedAt
            ?: return true
        val now = Clock.System.now()
        return now - oldestCache > query.timeToLive
    }
}