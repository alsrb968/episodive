package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.data.util.Cacher
import io.jacob.episodive.core.data.util.FeedQuery
import io.jacob.episodive.core.data.util.RecentFeedRemoteUpdater
import io.jacob.episodive.core.data.util.RecentNewFeedRemoteUpdater
import io.jacob.episodive.core.data.util.SoundbiteRemoteUpdater
import io.jacob.episodive.core.data.util.TrendingFeedRemoteUpdater
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.mapper.toRecentFeeds
import io.jacob.episodive.core.database.mapper.toRecentNewFeeds
import io.jacob.episodive.core.database.mapper.toSoundbites
import io.jacob.episodive.core.database.mapper.toTrendingFeeds
import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.TrendingFeed
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Instant

class FeedRepositoryImpl @Inject constructor(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
) : FeedRepository {
    override fun getTrendingFeeds(
        max: Int?,
        since: Instant?,
        language: String?,
        includeCategories: List<Category>,
        excludeCategories: List<Category>,
    ): Flow<List<TrendingFeed>> {
        val query = FeedQuery.Trending(
            language = language,
            categories = includeCategories
        )

        return Cacher(
            remoteUpdater = TrendingFeedRemoteUpdater(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                query = query
            ),
            sourceFactory = {
                localDataSource.getTrendingFeedsByCacheKey(query.key)
            }
        ).flow.map { it.toTrendingFeeds() }
    }

    override fun getRecentFeeds(
        max: Int?,
        since: Instant?,
        language: String?,
        includeCategories: List<Category>,
        excludeCategories: List<Category>,
    ): Flow<List<RecentFeed>> {
        val query = FeedQuery.Recent(
            language = language,
            categories = includeCategories
        )

        return Cacher(
            remoteUpdater = RecentFeedRemoteUpdater(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                query = query
            ),
            sourceFactory = {
                localDataSource.getRecentFeedsByCacheKey(query.key)
            }
        ).flow.map { it.toRecentFeeds() }
    }

    override fun getRecentNewFeeds(
        max: Int?,
        since: Instant?,
    ): Flow<List<RecentNewFeed>> {
        val query = FeedQuery.RecentNew

        return Cacher(
            remoteUpdater = RecentNewFeedRemoteUpdater(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                query = query
            ),
            sourceFactory = {
                localDataSource.getRecentNewFeedsByCacheKey(query.key)
            }
        ).flow.map { it.toRecentNewFeeds() }
    }

    override fun getRecentSoundbites(max: Int?): Flow<List<Soundbite>> {
        val query = FeedQuery.Soundbite

        return Cacher(
            remoteUpdater = SoundbiteRemoteUpdater(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                query = query
            ),
            sourceFactory = {
                localDataSource.getSoundbitesByCacheKey(query.key)
            }
        ).flow.map { it.toSoundbites() }
    }
}