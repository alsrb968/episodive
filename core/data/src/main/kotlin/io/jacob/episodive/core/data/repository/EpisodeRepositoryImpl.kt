package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.mapper.toCommaString
import io.jacob.episodive.core.network.mapper.toEpisode
import io.jacob.episodive.core.network.mapper.toEpisodes
import io.jacob.episodive.core.network.mapper.toLong
import javax.inject.Inject
import kotlin.time.Instant

class EpisodeRepositoryImpl @Inject constructor(
    private val episodeRemoteDataSource: EpisodeRemoteDataSource,
) : EpisodeRepository {
    override suspend fun searchEpisodesByPerson(
        person: String,
        max: Int?
    ): List<Episode> {
        return episodeRemoteDataSource.searchEpisodesByPerson(
            person = person,
            max = max,
        ).toEpisodes()
    }

    override suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int?,
        since: Instant?
    ): Pair<List<Episode>?, List<Episode>> {
        val ret = episodeRemoteDataSource.getEpisodesByFeedId(
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
        return episodeRemoteDataSource.getEpisodesByFeedUrl(
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
        return episodeRemoteDataSource.getEpisodesByPodcastGuid(
            guid = guid,
            max = max,
            since = since?.toLong(),
        ).toEpisodes()
    }

    override suspend fun getEpisodeById(id: Long): Episode? {
        return episodeRemoteDataSource.getEpisodeById(id = id)?.toEpisode()
    }

    override suspend fun getLiveEpisodes(max: Int?): List<Episode> {
        return episodeRemoteDataSource.getLiveEpisodes(max = max).toEpisodes()
    }

    override suspend fun getRandomEpisodes(
        max: Int?,
        language: String?,
        includeCategories: List<Category>,
        excludeCategories: List<Category>
    ): List<Episode> {
        return episodeRemoteDataSource.getRandomEpisodes(
            max = max,
            language = language,
            includeCategories = includeCategories.toCommaString(),
            excludeCategories = excludeCategories.toCommaString()
        ).toEpisodes()
    }

    override suspend fun getRecentEpisodes(
        max: Int?,
        excludeString: String?
    ): List<Episode> {
        return episodeRemoteDataSource.getRecentEpisodes(
            max = max,
            excludeString = excludeString
        ).toEpisodes()
    }
}