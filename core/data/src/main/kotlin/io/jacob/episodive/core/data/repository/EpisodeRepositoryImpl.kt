package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.data.mapper.toCommaString
import io.jacob.episodive.core.data.mapper.toEpisode
import io.jacob.episodive.core.data.mapper.toEpisodes
import io.jacob.episodive.core.data.mapper.toLong
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.network.NetworkDataSource
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class EpisodeRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
) : EpisodeRepository {
    override suspend fun searchEpisodesByPerson(
        person: String,
        max: Int?
    ): List<Episode> {
        return networkDataSource.searchEpisodesByPerson(
            person = person,
            max = max,
        ).toEpisodes()
    }

    override suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int?,
        since: Instant?
    ): Pair<List<Episode>?, List<Episode>> {
        val ret = networkDataSource.getEpisodesByFeedId(
            feedId = feedId,
            max = max,
            since = since?.toLong(),
        )
        return Pair(
            ret.first?.toEpisodes(),
            ret.second.toEpisodes()
        )
    }

    override suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int?,
        since: Instant?
    ): List<Episode> {
        return networkDataSource.getEpisodesByFeedUrl(
            feedUrl = feedUrl,
            max = max,
            since = since?.toLong(),
        ).toEpisodes()
    }

    override suspend fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int?,
        since: Instant?
    ): List<Episode> {
        return networkDataSource.getEpisodesByPodcastGuid(
            guid = guid,
            max = max,
            since = since?.toLong(),
        ).toEpisodes()
    }

    override suspend fun getEpisodeById(id: Long): Episode? {
        return networkDataSource.getEpisodeById(id = id)?.toEpisode()
    }

    override suspend fun getLiveEpisodes(max: Int?): List<Episode> {
        return networkDataSource.getLiveEpisodes(max = max).toEpisodes()
    }

    override suspend fun getRandomEpisodes(
        max: Int?,
        language: String?,
        includeCategories: List<Category>?,
        excludeCategories: List<Category>?
    ): List<Episode> {
        return networkDataSource.getRandomEpisodes(
            max = max,
            language = language,
            includeCategories = includeCategories?.toCommaString(),
            excludeCategories = excludeCategories?.toCommaString()
        ).toEpisodes()
    }

    override suspend fun getRecentEpisodes(
        max: Int?,
        excludeString: String?
    ): List<Episode> {
        return networkDataSource.getRecentEpisodes(
            max = max,
            excludeString = excludeString
        ).toEpisodes()
    }
}