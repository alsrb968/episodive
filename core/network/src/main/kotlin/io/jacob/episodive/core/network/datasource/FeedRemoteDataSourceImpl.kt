package io.jacob.episodive.core.network.datasource

import io.jacob.episodive.core.network.api.FeedApi
import io.jacob.episodive.core.network.model.RecentFeedResponse
import io.jacob.episodive.core.network.model.RecentNewFeedResponse
import io.jacob.episodive.core.network.model.RecentNewValueFeedResponse
import io.jacob.episodive.core.network.model.SoundbiteResponse
import io.jacob.episodive.core.network.model.TrendingFeedResponse
import timber.log.Timber
import javax.inject.Inject

class FeedRemoteDataSourceImpl @Inject constructor(
    val feedApi: FeedApi,
) : FeedRemoteDataSource {
    override suspend fun getTrendingFeeds(
        max: Int?,
        since: Long?,
        language: String?,
        includeCategories: String?,
        excludeCategories: String?,
    ): List<TrendingFeedResponse> {
        val ret = feedApi.getTrendingFeeds(
            max = max,
            since = since,
            language = language,
            includeCategories = includeCategories,
            excludeCategories = excludeCategories,
        ).dataList
        ret.forEachIndexed { index, response ->
            Timber.w("[$index] title: ${response.title}, image: ${response.image}" )
        }
        return ret
    }

    override suspend fun getRecentFeeds(
        max: Int?,
        since: Long?,
        language: String?,
        includeCategories: String?,
        excludeCategories: String?,
    ): List<RecentFeedResponse> {
        val ret = feedApi.getRecentFeeds(
            max = max,
            since = since,
            language = language,
            includeCategories = includeCategories,
            excludeCategories = excludeCategories,
        ).dataList
        ret.forEachIndexed { index, response ->
            Timber.w("[$index] title: ${response.title}, image: ${response.image}" )
        }
        return ret
    }

    override suspend fun getRecentNewFeeds(
        max: Int?,
        since: Long?,
    ): List<RecentNewFeedResponse> {
        return feedApi.getRecentNewFeeds(
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getRecentNewValueFeeds(
        max: Int?,
        since: Long?,
    ): List<RecentNewValueFeedResponse> {
        return feedApi.getRecentNewValueFeeds(
            max = max,
            since = since,
        ).dataList
    }

    override suspend fun getRecentSoundbites(max: Int?): List<SoundbiteResponse> {
        return feedApi.getRecentSoundbites(max = max).dataList
    }
}