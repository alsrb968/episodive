package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.data.util.cache.Cacher
import io.jacob.episodive.core.data.util.query.PodcastQuery
import io.jacob.episodive.core.data.util.updater.PodcastRemoteUpdater
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.mapper.toFollowedPodcasts
import io.jacob.episodive.core.database.mapper.toPodcasts
import io.jacob.episodive.core.database.model.FollowedPodcastEntity
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.FollowedPodcast
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.mapper.toPodcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Clock

class PodcastRepositoryImpl @Inject constructor(
    private val localDataSource: PodcastLocalDataSource,
    private val remoteDataSource: PodcastRemoteDataSource,
    private val remoteUpdater: PodcastRemoteUpdater.Factory,
) : PodcastRepository {
    override fun searchPodcasts(
        query: String,
        max: Int?,
    ): Flow<List<Podcast>> {
        val query = PodcastQuery.Search(query)

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getPodcastsByCacheKey(query.key)
            }
        ).flow.map { it.toPodcasts() }
    }

    override fun getPodcastByFeedId(feedId: Long): Flow<Podcast?> = flow {
        val podcast = remoteDataSource.getPodcastByFeedId(
            feedId = feedId
        )?.toPodcast()

        emit(podcast)
    }

    override fun getPodcastByFeedUrl(feedUrl: String): Flow<Podcast?> = flow {
        val podcast = remoteDataSource.getPodcastByFeedUrl(
            feedUrl = feedUrl
        )?.toPodcast()

        emit(podcast)
    }

    override fun getPodcastByGuid(guid: String): Flow<Podcast?> = flow {
        val podcast = remoteDataSource.getPodcastByGuid(
            guid = guid
        )?.toPodcast()

        emit(podcast)
    }

    override fun getPodcastsByMedium(
        medium: String,
        max: Int?,
    ): Flow<List<Podcast>> {
        val query = PodcastQuery.Medium(medium)

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getPodcastsByCacheKey(query.key)
            }
        ).flow.map { it.toPodcasts() }
    }

    override fun getFollowedPodcasts(): Flow<List<FollowedPodcast>> {
        return localDataSource.getFollowedPodcasts().map { it.toFollowedPodcasts() }
    }

    override suspend fun toggleFollowed(id: Long): Boolean {
        return if (localDataSource.isFollowed(id).first()) {
            localDataSource.removeFollowed(id)
            false
        } else {
            localDataSource.addFollowed(
                FollowedPodcastEntity(
                    id = id,
                    followedAt = Clock.System.now(),
                    isNotificationEnabled = true,
                )
            )
            true
        }
    }
}