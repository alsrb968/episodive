package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.mapper.toPodcast
import io.jacob.episodive.core.network.mapper.toPodcasts
import javax.inject.Inject

class PodcastRepositoryImpl @Inject constructor(
    private val podcastRemoteDataSource: PodcastRemoteDataSource,
) : PodcastRepository {
    override suspend fun searchPodcasts(
        query: String,
        max: Int?,
    ): List<Podcast> {
        return podcastRemoteDataSource.searchPodcasts(
            query = query,
            max = max,
        ).toPodcasts()
    }

    override suspend fun getPodcastByFeedId(feedId: Long): Podcast? {
        return podcastRemoteDataSource.getPodcastByFeedId(feedId = feedId)?.toPodcast()
    }

    override suspend fun getPodcastByFeedUrl(feedUrl: String): Podcast? {
        return podcastRemoteDataSource.getPodcastByFeedUrl(feedUrl = feedUrl)?.toPodcast()
    }

    override suspend fun getPodcastByGuid(guid: String): Podcast? {
        return podcastRemoteDataSource.getPodcastByGuid(guid = guid)?.toPodcast()
    }

    override suspend fun getPodcastsByMedium(
        medium: String,
        max: Int?,
    ): List<Podcast> {
        return podcastRemoteDataSource.getPodcastsByMedium(
            medium = medium,
            max = max,
        ).toPodcasts()
    }
}