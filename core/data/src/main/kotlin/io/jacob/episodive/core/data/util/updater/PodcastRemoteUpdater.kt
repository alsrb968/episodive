package io.jacob.episodive.core.data.util.updater

import androidx.paging.PagingSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.data.util.query.PodcastQuery
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.mapper.toPodcastEntities
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.database.model.PodcastWithExtrasView
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.mapper.toPodcasts
import io.jacob.episodive.core.network.model.PodcastResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlin.time.Instant
import timber.log.Timber

class PodcastRemoteUpdater @AssistedInject constructor(
    private val podcastLocal: PodcastLocalDataSource,
    private val podcastRemote: PodcastRemoteDataSource,
    private val feedRemote: FeedRemoteDataSource,
    @Assisted("query") override val query: PodcastQuery,
    backgroundRefresher: BackgroundRefresher,
) : RemoteUpdater<PodcastQuery, PodcastResponse, PodcastEntity, PodcastWithExtrasView>(query, backgroundRefresher) {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: PodcastQuery): PodcastRemoteUpdater
    }

    override suspend fun fetchFromRemote(fetchSize: Int): List<PodcastResponse> {
        return when (query) {
            is PodcastQuery.FeedId -> podcastRemote.getPodcastByFeedId(query.feedId)
                ?.let { listOf(it) } ?: emptyList()

            is PodcastQuery.FeedUrl -> podcastRemote.getPodcastByFeedUrl(query.feedUrl)
                ?.let { listOf(it) } ?: emptyList()

            is PodcastQuery.FeedGuid -> podcastRemote.getPodcastByGuid(query.feedGuid)
                ?.let { listOf(it) } ?: emptyList()

            is PodcastQuery.Medium -> podcastRemote.getPodcastsByMedium(query.medium, fetchSize)
            is PodcastQuery.ByChannel -> podcastRemote.getPodcastsByGuids(query.channel.podcastGuids)

            is PodcastQuery.Trending -> {
                feedRemote.getTrendingFeeds(
                    max = query.max,
                    language = query.language,
                    includeCategories = query.categories.toCommaString(),
                ).asFlow()
                    .flatMapMerge(concurrency = 10) { trend ->
                        // 항목마다 잡는다. 상세 요청 하나가 5xx·타임아웃을 내면 나머지가 다
                        // 성공했어도 목록 전체가 실패로 끝나고 한 건도 저장되지 않는다.
                        // 전체 목록(max=50)은 요청이 미리보기의 다섯 배라 그만큼 자주 걸린다.
                        flow { emit(podcastRemote.getPodcastByFeedId(trend.id)) }
                            .catch { e -> Timber.w(e, "피드 상세를 건너뛴다 (id=${trend.id})") }
                    }
                    .filterNotNull()
                    .toList()
            }

            is PodcastQuery.Recent -> {
                feedRemote.getRecentFeeds(
                    max = query.max,
                    language = query.language,
                    includeCategories = query.categories.toCommaString(),
                ).asFlow()
                    .flatMapMerge(concurrency = 10) { recent ->
                        flow { emit(podcastRemote.getPodcastByFeedId(recent.id)) }
                            .catch { e -> Timber.w(e, "피드 상세를 건너뛴다 (id=${recent.id})") }
                    }
                    .filterNotNull()
                    .toList()
            }

            is PodcastQuery.RecentNew -> {
                feedRemote.getRecentNewFeeds(max = query.max)
                    .asFlow()
                    .flatMapMerge(concurrency = 10) { recentNew ->
                        flow { emit(podcastRemote.getPodcastByFeedId(recentNew.id)) }
                            .catch { e -> Timber.w(e, "피드 상세를 건너뛴다 (id=${recentNew.id})") }
                    }
                    .filterNotNull()
                    .toList()
            }

            is PodcastQuery.Recommended -> coroutineScope {
                val trending = async {
                    feedRemote.getTrendingFeeds(
                        max = query.max / 2,
                        language = query.language,
                        includeCategories = query.categories.toCommaString(),
                    ).map { it.id to it.newestItemPublishTime }
                }
                val recent = async {
                    feedRemote.getRecentFeeds(
                        max = query.max / 2,
                        language = query.language,
                        includeCategories = query.categories.toCommaString(),
                    ).map { it.id to it.newestItemPublishTime }
                }

                val recommend = (trending.await() + recent.await())
                    .distinctBy { it.first }
                    .sortedByDescending { it.second }

                recommend.map { it.first }
                    .asFlow()
                    .withIndex()
                    .flatMapMerge(concurrency = 5) { (index, id) ->
                        flow {
                            podcastRemote.getPodcastByFeedId(id)?.let { podcast ->
                                emit(index to podcast)
                            }
                        }.catch { e ->
                            e.printStackTrace()
                        }
                    }
                    .toList()
                    .sortedBy { it.first }
                    .map { it.second }
            }
        }
    }

    override suspend fun convertToEntity(responses: List<PodcastResponse>): List<PodcastEntity> {
        return responses.toPodcasts().toPodcastEntities()
    }

    override suspend fun replaceToLocal(entities: List<PodcastEntity>) {
        podcastLocal.replacePodcasts(entities, query.key)
    }

    override suspend fun getOldestCachedAt(): Instant? =
        podcastLocal.getOldestCreatedAtByGroupKey(query.key)

    override fun getPagingSource(): PagingSource<Int, PodcastWithExtrasView> {
        return podcastLocal.getPodcastsByGroupKeyPaging(query.key)
    }

    override fun getFlowSource(count: Int): Flow<List<PodcastWithExtrasView>> {
        return podcastLocal.getPodcastsByGroupKey(query.key, count)
    }
}
