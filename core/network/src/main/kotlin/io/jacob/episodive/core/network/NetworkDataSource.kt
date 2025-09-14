package io.jacob.episodive.core.network

import io.jacob.episodive.core.network.api.PodcastIndexApi
import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.network.model.PodcastResponse
import io.jacob.episodive.core.network.model.RecentFeedResponse
import io.jacob.episodive.core.network.model.RecentNewFeedResponse
import io.jacob.episodive.core.network.model.RecentNewValueFeedResponse
import io.jacob.episodive.core.network.model.SoundbiteResponse
import javax.inject.Inject

interface NetworkDataSource {
    suspend fun searchPodcasts(
        query: String,
        max: Int? = null,
    ): List<PodcastResponse>

    suspend fun searchEpisodesByPerson(
        person: String,
        max: Int? = null,
    ): List<EpisodeResponse>

    suspend fun getPodcastByFeedId(feedId: Long): PodcastResponse?

    suspend fun getPodcastByFeedUrl(feedUrl: String): PodcastResponse?

    suspend fun getPodcastByGuid(guid: String): PodcastResponse?

    suspend fun getPodcastsByMedium(
        medium: String,
        max: Int? = null,
    ): List<PodcastResponse>

    suspend fun getTrendingPodcasts(
        max: Int? = null,
        since: Long? = null,
        language: String? = null,
        includeCategories: String? = null,
        excludeCategories: String? = null,
    ): List<PodcastResponse>

    suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int? = null,
        since: Long? = null,
    ): Pair<List<EpisodeResponse>?, List<EpisodeResponse>>

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

    suspend fun getRecentFeeds(
        max: Int? = null,
        since: Long? = null,
        language: String? = null,
        includeCategories: String? = null,
        excludeCategories: String? = null,
    ): List<RecentFeedResponse>

    suspend fun getRecentNewFeeds(
        max: Int? = null,
        since: Long? = null,
    ): List<RecentNewFeedResponse>

    suspend fun getRecentNewValueFeeds(
        max: Int? = null,
        since: Long? = null,
    ): List<RecentNewValueFeedResponse>

    suspend fun getRecentSoundbites(max: Int? = null): List<SoundbiteResponse>
}

class NetworkDataSourceImpl @Inject constructor(
    val api: PodcastIndexApi,
) : NetworkDataSource {
    override suspend fun searchPodcasts(
        query: String,
        max: Int?,
    ): List<PodcastResponse> {
        return api.searchPodcasts(
            query = query,
            max = max,
        ).dataList
    }

    override suspend fun searchEpisodesByPerson(
        person: String,
        max: Int?,
    ): List<EpisodeResponse> {
        return api.searchEpisodesByPerson(
            person = person,
            max = max,
        ).dataList
    }

    override suspend fun getPodcastByFeedId(feedId: Long): PodcastResponse? {
        return api.getPodcastByFeedId(feedId = feedId).data
    }

    override suspend fun getPodcastByFeedUrl(feedUrl: String): PodcastResponse? {
        return api.getPodcastByFeedUrl(feedUrl = feedUrl).data
    }

    override suspend fun getPodcastByGuid(guid: String): PodcastResponse? {
        return api.getPodcastByGuid(guid = guid).data
    }

    override suspend fun getPodcastsByMedium(
        medium: String,
        max: Int?,
    ): List<PodcastResponse> {
        return api.getPodcastsByMedium(
            medium = medium,
            max = max,
        ).dataList
    }

    override suspend fun getTrendingPodcasts(
        max: Int?,
        since: Long?,
        language: String?,
        includeCategories: String?,
        excludeCategories: String?,
    ): List<PodcastResponse> {
        return api.getTrendingPodcasts(
            max = max,
            since = since,
            language = language,
            includeCategories = includeCategories,
            excludeCategories = excludeCategories,
        ).dataList
    }

    override suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int?,
        since: Long?,
    ): Pair<List<EpisodeResponse>?, List<EpisodeResponse>> {
        val response = api.getEpisodesByFeedId(
            feedId = feedId,
            max = max,
            since = since,
        )
        return response.liveEpisodes to response.dataList
    }

    override suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int?,
        since: Long?,
    ): List<EpisodeResponse> {
        return api.getEpisodesByFeedUrl(
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
        return api.getEpisodesByPodcastGuid(
            guid = guid,
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getEpisodeById(id: Long): EpisodeResponse? {
        return api.getEpisodeById(id = id).data
    }

    override suspend fun getLiveEpisodes(max: Int?): List<EpisodeResponse> {
        return api.getLiveEpisodes(max = max).dataList
    }

    override suspend fun getRandomEpisodes(
        max: Int?,
        language: String?,
        includeCategories: String?,
        excludeCategories: String?,
    ): List<EpisodeResponse> {
        return api.getRandomEpisodes(
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
        return api.getRecentEpisodes(
            max = max,
            excludeString = excludeString,
        ).dataList
    }

    override suspend fun getRecentFeeds(
        max: Int?,
        since: Long?,
        language: String?,
        includeCategories: String?,
        excludeCategories: String?,
    ): List<RecentFeedResponse> {
        return api.getRecentFeeds(
            max = max,
            since = since,
            language = language,
            includeCategories = includeCategories,
            excludeCategories = excludeCategories,
        ).dataList
    }

    override suspend fun getRecentNewFeeds(
        max: Int?,
        since: Long?,
    ): List<RecentNewFeedResponse> {
        return api.getRecentNewFeeds(
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getRecentNewValueFeeds(
        max: Int?,
        since: Long?,
    ): List<RecentNewValueFeedResponse> {
        return api.getRecentNewValueFeeds(
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getRecentSoundbites(max: Int?): List<SoundbiteResponse> {
        return api.getRecentSoundbites(max = max).dataList
    }
}