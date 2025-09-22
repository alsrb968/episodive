package io.jacob.episodive.core.data.util

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toTrendingFeedEntities
import io.jacob.episodive.core.database.model.TrendingFeedEntity
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.mapper.toTrendingFeeds

class TrendingFeedRemoteUpdater @AssistedInject constructor(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    @Assisted("query") private val query: FeedQuery,
) : RemoteUpdater<TrendingFeedEntity> {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: FeedQuery): TrendingFeedRemoteUpdater
    }

    override suspend fun load(cached: List<TrendingFeedEntity>) {
        try {
            if (cached.isTrendingFeedsExpired(query.timeToLive)) {
                when (query) {
                    is FeedQuery.Trending -> {
                        val networkResult = remoteDataSource.getTrendingFeeds(
                            language = query.language,
                            includeCategories = query.categories.toCommaString(),
                        )
                        val feeds = networkResult.toTrendingFeeds()
                        localDataSource.upsertTrendingFeeds(feeds.toTrendingFeedEntities(query.key))
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}