package io.jacob.episodive.core.network.api

import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.network.model.ResponseListWrapper
import io.jacob.episodive.core.network.model.ResponseWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface EpisodeApi {
    @GET("search/byperson")
    suspend fun searchEpisodesByPerson(
        @Query("q") person: String,
        @Query("max") max: Int? = null,
    ): ResponseListWrapper<EpisodeResponse>

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
}