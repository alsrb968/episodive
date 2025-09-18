package io.jacob.episodive.core.network.datasource

import io.jacob.episodive.core.network.model.EpisodeResponse

interface EpisodeRemoteDataSource {
    suspend fun searchEpisodesByPerson(
        person: String,
        max: Int? = null,
    ): List<EpisodeResponse>

    suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int? = null,
        since: Long? = null,
    ): List<EpisodeResponse>

    suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int? = null,
        since: Long? = null,
    ): List<EpisodeResponse>

    suspend fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int? = null,
        since: Long? = null,
    ): List<EpisodeResponse>

    suspend fun getEpisodeById(id: Long): EpisodeResponse?

    suspend fun getLiveEpisodes(max: Int? = null): List<EpisodeResponse>

    suspend fun getRandomEpisodes(
        max: Int? = null,
        language: String? = null,
        includeCategories: String? = null,
        excludeCategories: String? = null,
    ): List<EpisodeResponse>

    suspend fun getRecentEpisodes(
        max: Int? = null,
        excludeString: String? = null,
    ): List<EpisodeResponse>
}