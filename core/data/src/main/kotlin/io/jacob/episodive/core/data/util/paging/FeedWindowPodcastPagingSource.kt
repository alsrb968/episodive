package io.jacob.episodive.core.data.util.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.mapper.toFeedEntities
import io.jacob.episodive.core.database.mapper.toPodcastEntity
import io.jacob.episodive.core.database.model.PodcastWithExtrasView
import io.jacob.episodive.core.model.Feed
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.mapper.toPodcast
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import timber.log.Timber
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * 피드 목록을 먼저 통째로 받아 두고, **지금 보고 있는 페이지에 해당하는 팟캐스트만** 상세를
 * 채우는 PagingSource.
 *
 * Podcast Index 의 목록 API 는 피드 요약만 준다. 카드에 필요한 것(설명·에피소드 수 등)을 얻으려면
 * 피드마다 상세를 한 번 더 불러야 한다. `PodcastRemoteUpdater` 는 목록 전체를 미리 채우므로,
 * 50개짜리 전체 목록의 첫 화면이 **1 + 50 번의 요청**이 끝날 때까지 스켈레톤에 머문다.
 * 여기서는 첫 화면에 보이는 만큼만 채워 왕복을 둘로 줄인다 — 피드 목록 1회, 첫 페이지 상세
 * 1회(동시 실행).
 *
 * 대신 이 방식은 피드 목록을 그룹별로 나란히 들고 있어야 한다. `feeds` 의 기본키가 `id` 단독일
 * 때는 한 피드가 한 그룹에만 존재할 수 있어 트렌딩 목록을 채우면 추천 목록이 무너졌다 —
 * 그래서 복합키 마이그레이션(v12) 전까지 이 방식은 추천 한 곳에서만 쓸 수 있었다.
 *
 * @param groupKey `feeds` 와 `podcast_group` 양쪽에서 이 목록을 가리키는 키. 호출자가
 *   `PodcastQuery.key` 를 그대로 넘겨 캐시 키 생성 지점을 하나로 유지한다.
 * @param timeToLive 피드 목록의 수명. 팟캐스트 상세는 이 수명과 무관하게 한 번 받으면 재사용한다.
 * @param fetchFeeds 목록을 가져오는 방법. **반환 순서가 그대로 화면 순서가 된다** —
 *   [toFeedEntities] 가 위치를 `sortOrder` 에 박는다.
 */
class FeedWindowPodcastPagingSource(
    private val podcastLocal: PodcastLocalDataSource,
    private val podcastRemote: PodcastRemoteDataSource,
    private val feedLocal: FeedLocalDataSource,
    private val groupKey: String,
    private val timeToLive: Duration,
    private val fetchFeeds: suspend () -> List<Feed>,
) : PagingSource<Int, PodcastWithExtrasView>() {

    override fun getRefreshKey(state: PagingState<Int, PodcastWithExtrasView>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PodcastWithExtrasView> {
        return try {
            ensureFeedsAreFresh()

            val offset = params.key ?: 0
            val limit = params.loadSize

            val feeds = feedLocal.getFeedsPagingList(
                groupKey = groupKey,
                offset = offset,
                limit = limit,
            )

            if (feeds.isEmpty()) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (offset > 0) offset - limit else null,
                    nextKey = null,
                )
            }

            val podcastIds = feeds.map { it.id }

            fetchMissingPodcasts(podcastIds)

            // 상세를 못 받은 피드는 조용히 빠진다. 한 건의 실패로 페이지 전체를 오류로 만들지
            // 않기 위해서다 — 대신 그 자리는 비고, 다음 새로고침에서 다시 시도한다.
            val allPodcasts = podcastLocal.getPodcastsByIdsOnce(podcastIds)
            val orderedPodcasts = podcastIds.mapNotNull { id ->
                allPodcasts.find { it.podcast.id == id }
            }

            LoadResult.Page(
                data = orderedPodcasts,
                prevKey = if (offset > 0) offset - limit else null,
                // 다음 키는 채워진 팟캐스트 수가 아니라 **피드 수**로 정한다. 상세 요청이
                // 실패해 데이터가 비어도 목록은 계속 이어져야 한다.
                nextKey = if (feeds.size == limit) offset + limit else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun ensureFeedsAreFresh() {
        val oldestCachedAt = feedLocal.getFeedsOldestCachedAt(groupKey)
        val isExpired = oldestCachedAt?.let {
            Clock.System.now() - it > timeToLive
        } ?: true

        if (!isExpired) return

        val feedEntities = try {
            fetchFeeds().toFeedEntities(groupKey = groupKey)
        } catch (e: Exception) {
            // 갱신에 실패해도 캐시가 있으면 낡은 목록을 그대로 보여준다. `RemoteUpdater` 의
            // stale-while-error 와 같은 정책이다 — 목록이 이미 있는데 새로고침 한 번 실패했다고
            // 화면을 통째로 재시도 버튼으로 바꾸지 않는다. 보여줄 것이 없을 때만 전파한다.
            if (oldestCachedAt == null) throw e
            Timber.w(e, "피드 목록 갱신에 실패해 캐시를 유지한다 (key=$groupKey)")
            return
        }

        feedLocal.replaceFeedsByGroupKey(feedEntities, groupKey)
        // 피드 목록이 바뀌면 이 그룹의 팟캐스트 매핑도 버린다. 안 버리면 새 목록에 없는
        // 팟캐스트가 그룹에 남아 개수 제한만 잡아먹는다. 팟캐스트 행 자체는 다른 그룹·팔로우가
        // 참조하면 살아남는다.
        podcastLocal.replacePodcasts(emptyList(), groupKey)
        Timber.d("피드 목록을 새로 받았다 (key=$groupKey, size=${feedEntities.size})")
    }

    private suspend fun fetchMissingPodcasts(podcastIds: List<Long>) {
        val cachedPodcastIds = podcastLocal.getPodcastsByIdsOnce(podcastIds)
            .map { it.podcast.id }
            .toSet()
        val missingPodcastIds = podcastIds.filterNot { it in cachedPodcastIds }

        if (missingPodcastIds.isEmpty()) return

        val podcastEntities = missingPodcastIds
            .withIndex()
            .asFlow()
            .flatMapMerge(
                concurrency = missingPodcastIds.size.coerceAtMost(MAX_HYDRATION_CONCURRENCY),
            ) { (index, podcastId) ->
                flow {
                    // 항목마다 잡는다. 상세 하나가 5xx·타임아웃을 내도 나머지 페이지는 뜬다.
                    try {
                        podcastRemote.getPodcastByFeedId(podcastId)?.let {
                            emit(index to it.toPodcast().toPodcastEntity())
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "피드 상세를 건너뛴다 (id=$podcastId)")
                    }
                }
            }
            .toList()
            .sortedBy { it.first }
            .map { it.second }

        if (podcastEntities.isNotEmpty()) {
            podcastLocal.upsertPodcastsWithGroup(
                podcasts = podcastEntities,
                groupKey = groupKey,
            )
        }
    }

    companion object {
        /**
         * 한 페이지를 채우는 동시 요청 수의 상한.
         *
         * 페이지 크기만큼 동시에 쏘는 것이 첫 화면에는 가장 빠르지만, 새로고침이 큰
         * `loadSize` 로 들어올 때까지 그대로 두면 상한 없이 늘어난다. 목록 전체를 미리 채우던
         * 시절 `PodcastRemoteUpdater` 가 쓰던 값과 같게 잡는다.
         */
        private const val MAX_HYDRATION_CONCURRENCY = 10
    }
}
