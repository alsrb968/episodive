package io.jacob.episodive.core.network.api

import io.jacob.episodive.core.network.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedApi {
    @GET("podcasts/trending")
    suspend fun getTrendingFeeds(
        @Query("max") max: Int? = null,
        @Query("since") since: Long? = null,
        @Query("lang") language: String? = null,
        @Query("cat") includeCategories: String? = null,
        @Query("notcat") excludeCategories: String? = null,
    ): ResponseListWrapper<TrendingFeedResponse>

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