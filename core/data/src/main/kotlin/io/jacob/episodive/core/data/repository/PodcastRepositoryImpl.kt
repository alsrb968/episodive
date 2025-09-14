package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.data.mapper.toCommaString
import io.jacob.episodive.core.data.mapper.toLong
import io.jacob.episodive.core.data.mapper.toPodcast
import io.jacob.episodive.core.data.mapper.toPodcasts
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.network.NetworkDataSource
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PodcastRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
) : PodcastRepository {
    override suspend fun searchPodcasts(
        query: String,
        max: Int?,
    ): List<Podcast> {
        return networkDataSource.searchPodcasts(
            query = query,
            max = max,
        ).toPodcasts()
    }

    override suspend fun getPodcastByFeedId(feedId: Long): Podcast? {
        return networkDataSource.getPodcastByFeedId(feedId = feedId)?.toPodcast()
    }

    override suspend fun getPodcastByFeedUrl(feedUrl: String): Podcast? {
        return networkDataSource.getPodcastByFeedUrl(feedUrl = feedUrl)?.toPodcast()
    }

    override suspend fun getPodcastByGuid(guid: String): Podcast? {
        return networkDataSource.getPodcastByGuid(guid = guid)?.toPodcast()
    }

    override suspend fun getPodcastsByMedium(
        medium: String,
        max: Int?,
    ): List<Podcast> {
        return networkDataSource.getPodcastsByMedium(
            medium = medium,
            max = max,
        ).toPodcasts()
    }

    override suspend fun getTrendingPodcasts(
        max: Int?,
        since: Instant?,
        language: String?,
        includeCategories: List<Category>?,
        excludeCategories: List<Category>?,
    ): List<Podcast> {
        return networkDataSource.getTrendingPodcasts(
            max = max,
            since = since?.toLong(),
            language = language,
            includeCategories = includeCategories?.toCommaString(),
            excludeCategories = excludeCategories?.toCommaString()
        ).toPodcasts()
    }
}