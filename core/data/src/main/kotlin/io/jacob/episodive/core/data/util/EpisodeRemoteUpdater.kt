package io.jacob.episodive.core.data.util

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.mapper.toEpisodes

class EpisodeRemoteUpdater @AssistedInject constructor(
    private val localDataSource: EpisodeLocalDataSource,
    private val remoteDataSource: EpisodeRemoteDataSource,
    @Assisted("query") private val query: EpisodeQuery,
) : RemoteUpdater<EpisodeEntity> {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: EpisodeQuery): EpisodeRemoteUpdater
    }

    /**
     * 네트워크에서 데이터를 가져와 로컬 DB에 업데이트만 담당
     */
    override suspend fun load(cached: List<EpisodeEntity>) {
        try {
            if (cached.isEpisodesExpired(query.timeToLive)) {
                // 네트워크에서 데이터 로드
                val networkResult = when (query) {
                    is EpisodeQuery.Person -> remoteDataSource.searchEpisodesByPerson(query.person)
                    is EpisodeQuery.FeedId -> remoteDataSource.getEpisodesByFeedId(query.feedId)
                    is EpisodeQuery.FeedUrl -> remoteDataSource.getEpisodesByFeedUrl(query.feedUrl)
                    is EpisodeQuery.PodcastGuid -> remoteDataSource.getEpisodesByPodcastGuid(query.podcastGuid)
                    is EpisodeQuery.Live -> remoteDataSource.getLiveEpisodes()
                    is EpisodeQuery.Recent -> remoteDataSource.getRecentEpisodes()
                }
                val episodes = networkResult.toEpisodes()

                // 로컬 캐시 업데이트
                localDataSource.upsertEpisodes(episodes.toEpisodeEntities(query.key))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}