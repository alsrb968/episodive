package io.jacob.episodive.core.network.datasource

import io.jacob.episodive.core.network.api.EpisodeApi
import io.jacob.episodive.core.network.model.EpisodeResponse
import javax.inject.Inject

class EpisodeRemoteDataSourceImpl @Inject constructor(
    private val episodeApi: EpisodeApi
) : EpisodeRemoteDataSource {
    override suspend fun searchEpisodesByPerson(
        person: String,
        max: Int?,
    ): List<EpisodeResponse> {
        return episodeApi.searchEpisodesByPerson(
            person = person,
            max = max,
        ).dataList
    }

    override suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int?,
        since: Long?,
    ): List<EpisodeResponse> {
        val response = episodeApi.getEpisodesByFeedId(
            feedId = feedId,
            max = max,
            since = since,
        )
        return response.liveEpisodes?.plus(response.dataList) ?: response.dataList
    }

    override suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int?,
        since: Long?,
    ): List<EpisodeResponse> {
        return episodeApi.getEpisodesByFeedUrl(
            feedUrl = feedUrl,
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int?,
        since: Long?,
    ): List<EpisodeResponse> {
        return episodeApi.getEpisodesByPodcastGuid(
            guid = guid,
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getEpisodeById(id: Long): EpisodeResponse? {
        return episodeApi.getEpisodeById(id = id).data
    }

    override suspend fun getLiveEpisodes(max: Int?): List<EpisodeResponse> {
        return episodeApi.getLiveEpisodes(max = max).dataList
    }

    override suspend fun getRandomEpisodes(
        max: Int?,
        language: String?,
        includeCategories: String?,
        excludeCategories: String?,
    ): List<EpisodeResponse> {
        return episodeApi.getRandomEpisodes(
            max = max,
            language = language,
            includeCategories = includeCategories,
            excludeCategories = excludeCategories,
        ).dataList
    }

    override suspend fun getRecentEpisodes(
        max: Int?,
        excludeString: String?,
    ): List<EpisodeResponse> {
        return episodeApi.getRecentEpisodes(
            max = max,
            excludeString = excludeString,
        ).dataList
    }
}