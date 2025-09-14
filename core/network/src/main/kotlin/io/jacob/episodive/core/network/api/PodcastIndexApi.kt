package io.jacob.episodive.core.network.api

import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.network.model.PodcastResponse
import io.jacob.episodive.core.network.model.RecentFeedResponse
import io.jacob.episodive.core.network.model.RecentNewFeedResponse
import io.jacob.episodive.core.network.model.RecentNewValueFeedResponse
import io.jacob.episodive.core.network.model.ResponseListWrapper
import io.jacob.episodive.core.network.model.ResponseWrapper
import io.jacob.episodive.core.network.model.SoundbiteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PodcastIndexApi {
    @GET("search/byterm")
    suspend fun searchPodcasts(
        @Query("q") query: String,
        @Query("max") max: Int? = null,
    ): ResponseListWrapper<PodcastResponse>

    @GET("search/byperson")
    suspend fun searchEpisodesByPerson(
        @Query("q") person: String,
        @Query("max") max: Int? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("podcasts/byfeedid")
    suspend fun getPodcastByFeedId(
        @Query("id") feedId: Long,
    ): ResponseWrapper<PodcastResponse>

    @GET("podcasts/byfeedurl")
    suspend fun getPodcastByFeedUrl(
        @Query("url") feedUrl: String,
    ): ResponseWrapper<PodcastResponse>

    @GET("podcasts/byguid")
    suspend fun getPodcastByGuid(
        @Query("guid") guid: String,
    ): ResponseWrapper<PodcastResponse>

    @GET("podcasts/bymedium")
    suspend fun getPodcastsByMedium(
        @Query("medium") medium: String,
        @Query("max") max: Int? = null,
    ): ResponseListWrapper<PodcastResponse>

    @GET("podcasts/trending")
    suspend fun getTrendingPodcasts(
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
        @Query("lang") language: String? = null,
        @Query("cat") includeCategories: String? = null,
        @Query("notcat") excludeCategories: String? = null,
    ): ResponseListWrapper<PodcastResponse>

    @GET("episodes/byfeedid")
    suspend fun getEpisodesByFeedId(
        @Query("id") feedId: Long,
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("episodes/byfeedurl")
    suspend fun getEpisodesByFeedUrl(
        @Query("url") feedUrl: String,
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("episodes/bypodcastguid")
    suspend fun getEpisodesByPodcastGuid(
        @Query("guid") guid: String,
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("episodes/byid")
    suspend fun getEpisodeById(
        @Query("id") id: Long,
    ): ResponseWrapper<EpisodeResponse>

    @GET("episodes/live")
    suspend fun getLiveEpisodes(
        @Query("max") max: Int? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("episodes/random")
    suspend fun getRandomEpisodes(
        @Query("max") max: Int? = null,
        @Query("lang") language: String? = null,
        @Query("cat") includeCategories: String? = null,
        @Query("notcat") excludeCategories: String? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("recent/episodes")
    suspend fun getRecentEpisodes(
        @Query("max") max: Int? = null,
        @Query("excludeString") excludeString: String? = null,
    ): ResponseListWrapper<EpisodeResponse>

    @GET("recent/feeds")
    suspend fun getRecentFeeds(
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
        @Query("lang") language: String? = null,
        @Query("cat") includeCategories: String? = null,
        @Query("notcat") excludeCategories: String? = null,
    ): ResponseListWrapper<RecentFeedResponse>

    @GET("recent/newfeeds")
    suspend fun getRecentNewFeeds(
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
    ): ResponseListWrapper<RecentNewFeedResponse>

    @GET("recent/newvaluefeeds")
    suspend fun getRecentNewValueFeeds(
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
    ): ResponseListWrapper<RecentNewValueFeedResponse>

    @GET("recent/soundbites")
    suspend fun getRecentSoundbites(
        @Query("max") max: Int? = null,
    ): ResponseListWrapper<SoundbiteResponse>
}