package io.jacob.episodive.core.data.util.paging

import androidx.paging.PagingSource
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.model.FeedEntity
import io.jacob.episodive.core.database.model.PodcastWithExtrasView
import io.jacob.episodive.core.model.Feed
import io.jacob.episodive.core.model.GroupKey
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.jacob.episodive.core.testing.util.loadPage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class FeedWindowPodcastPagingSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastLocal = mockk<PodcastLocalDataSource>(relaxed = true)
    private val podcastRemote = mockk<PodcastRemoteDataSource>(relaxed = true)
    private val feedLocal = mockk<FeedLocalDataSource>(relaxed = true)

    private val defaultGroupKey = "${GroupKey.RECOMMENDED}:all:"

    private fun createPagingSource(
        groupKey: String = defaultGroupKey,
        timeToLive: Duration = 10.minutes,
        fetchFeeds: suspend () -> List<Feed> = { emptyList() },
    ) = FeedWindowPodcastPagingSource(
        podcastLocal = podcastLocal,
        podcastRemote = podcastRemote,
        feedLocal = feedLocal,
        groupKey = groupKey,
        timeToLive = timeToLive,
        fetchFeeds = fetchFeeds,
    )

    private fun mockPodcastWithExtrasView(id: Long): PodcastWithExtrasView =
        mockk(relaxed = true) {
            every { podcast } returns mockk(relaxed = true) {
                every { this@mockk.id } returns id
            }
        }

    private fun mockFeedEntity(id: Long): FeedEntity =
        mockk(relaxed = true) {
            every { this@mockk.id } returns id
        }

    private fun mockFeed(id: Long): Feed =
        mockk(relaxed = true) {
            every { this@mockk.id } returns id
        }

    @Test
    fun `Given fresh cache with all podcasts cached, When load, Then returns podcasts in feed order`() =
        runTest {
            // Given
            val feeds = listOf(
                mockFeedEntity(100L),
                mockFeedEntity(200L),
                mockFeedEntity(300L),
            )
            val podcasts = listOf(
                mockPodcastWithExtrasView(100L),
                mockPodcastWithExtrasView(200L),
                mockPodcastWithExtrasView(300L),
            )
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, 0, 10)
            } returns feeds
            coEvery {
                podcastLocal.getPodcastsByIdsOnce(listOf(100L, 200L, 300L))
            } returns podcasts

            // When
            val page = createPagingSource().loadPage(loadSize = 10)

            // Then
            assertEquals(3, page.data.size)
            assertNull(page.prevKey)
            assertEquals(100L, page.data[0].podcast.id)
            assertEquals(200L, page.data[1].podcast.id)
            assertEquals(300L, page.data[2].podcast.id)
        }

    @Test
    fun `Given expired cache, When load, Then refills feeds and clears the podcast group`() =
        runTest {
            // Given
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns null
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, any(), any())
            } returns emptyList()
            var fetched = false

            // When
            val page = createPagingSource(fetchFeeds = { fetched = true; listOf(mockFeed(1L)) })
                .loadPage(loadSize = 10)

            // Then
            assertEquals(0, page.data.size)
            assertTrue(fetched)
            coVerify { feedLocal.replaceFeedsByGroupKey(any(), defaultGroupKey) }
            coVerify { podcastLocal.replacePodcasts(emptyList(), defaultGroupKey) }
        }

    @Test
    fun `Given expired cache, When load, Then stores the fetched order as sortOrder`() =
        runTest {
            // 원격이 준 순위를 붙잡는 유일한 지점이다. 여기서 위치를 잃으면 아래 계층에는
            // 순서를 복원할 단서가 남지 않는다.
            // Given
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns null
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, any(), any())
            } returns emptyList()
            val stored = slot<List<FeedEntity>>()
            coEvery {
                feedLocal.replaceFeedsByGroupKey(capture(stored), defaultGroupKey)
            } returns Unit

            // When
            createPagingSource(
                fetchFeeds = { listOf(mockFeed(30L), mockFeed(10L), mockFeed(20L)) },
            ).loadPage(loadSize = 10)

            // Then
            assertEquals(listOf(30L, 10L, 20L), stored.captured.map { it.id })
            assertEquals(listOf(0, 1, 2), stored.captured.map { it.sortOrder })
        }

    @Test
    fun `Given fresh cache, When load, Then does not fetch feeds`() =
        runTest {
            // Given
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, any(), any())
            } returns emptyList()
            var fetched = false

            // When
            createPagingSource(fetchFeeds = { fetched = true; emptyList() }).loadPage(loadSize = 10)

            // Then
            assertEquals(false, fetched)
            coVerify(exactly = 0) { feedLocal.replaceFeedsByGroupKey(any(), any()) }
        }

    @Test
    fun `Given a failing refresh with a stale cache, When load, Then the stale list is served`() =
        runTest {
            // RemoteUpdater 와 같은 stale-while-error 정책. 목록이 이미 있는데 새로고침 한 번
            // 실패했다고 화면을 통째로 재시도 버튼으로 바꾸지 않는다.
            // Given
            coEvery {
                feedLocal.getFeedsOldestCachedAt(defaultGroupKey)
            } returns Clock.System.now() - 1.hours
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, 0, 10)
            } returns listOf(mockFeedEntity(100L))
            coEvery {
                podcastLocal.getPodcastsByIdsOnce(listOf(100L))
            } returns listOf(mockPodcastWithExtrasView(100L))

            // When
            val page = createPagingSource(fetchFeeds = { throw RuntimeException("offline") })
                .loadPage(loadSize = 10)

            // Then
            assertEquals(listOf(100L), page.data.map { it.podcast.id })
            coVerify(exactly = 0) { feedLocal.replaceFeedsByGroupKey(any(), any()) }
        }

    @Test
    fun `Given a failing refresh with no cache, When load, Then the error surfaces`() =
        runTest {
            // 반대쪽 절반이다. 보여줄 것이 하나도 없으면 조용히 빈 화면을 내는 대신 오류로
            // 올려, 화면이 재시도를 제안할 수 있게 한다.
            // Given
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns null

            // When
            val result = createPagingSource(fetchFeeds = { throw RuntimeException("offline") })
                .load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 10,
                        placeholdersEnabled = false,
                    )
                )

            // Then
            assertTrue(result is PagingSource.LoadResult.Error)
            assertEquals("offline", (result as PagingSource.LoadResult.Error).throwable.message)
        }

    @Test
    fun `Given missing podcasts, When load, Then fetches from remote and upserts`() =
        runTest {
            // Given
            val feeds = listOf(
                mockFeedEntity(100L),
                mockFeedEntity(200L),
                mockFeedEntity(300L),
            )
            val cachedPodcast = mockPodcastWithExtrasView(100L)
            val allPodcasts = listOf(
                mockPodcastWithExtrasView(100L),
                mockPodcastWithExtrasView(200L),
                mockPodcastWithExtrasView(300L),
            )

            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, 0, 10)
            } returns feeds
            // First call returns only 1 cached, second returns all
            coEvery {
                podcastLocal.getPodcastsByIdsOnce(listOf(100L, 200L, 300L))
            } returns listOf(cachedPodcast) andThen allPodcasts
            coEvery { podcastRemote.getPodcastByFeedId(any()) } returns mockk(relaxed = true)

            // When
            val page = createPagingSource().loadPage(loadSize = 10)

            // Then
            assertEquals(3, page.data.size)
            coVerify(exactly = 0) { podcastRemote.getPodcastByFeedId(100L) }
            coVerify { podcastRemote.getPodcastByFeedId(200L) }
            coVerify { podcastRemote.getPodcastByFeedId(300L) }
            coVerify {
                podcastLocal.upsertPodcastsWithGroup(any(), defaultGroupKey)
            }
        }

    @Test
    fun `Given a failing detail request, When load, Then the rest of the page still loads`() =
        runTest {
            // 상세 하나가 죽었다고 페이지 전체를 오류로 만들면, 목록 API 는 멀쩡한데 화면은
            // 통째로 재시도 버튼이 된다.
            // Given
            val feeds = listOf(mockFeedEntity(100L), mockFeedEntity(200L))
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery { feedLocal.getFeedsPagingList(defaultGroupKey, 0, 10) } returns feeds
            coEvery {
                podcastLocal.getPodcastsByIdsOnce(listOf(100L, 200L))
            } returns emptyList() andThen listOf(mockPodcastWithExtrasView(200L))
            coEvery { podcastRemote.getPodcastByFeedId(100L) } throws RuntimeException("boom")
            coEvery { podcastRemote.getPodcastByFeedId(200L) } returns mockk(relaxed = true)

            // When
            val page = createPagingSource().loadPage(loadSize = 10)

            // Then
            assertEquals(1, page.data.size)
            assertEquals(200L, page.data[0].podcast.id)
        }

    @Test
    fun `Given no feeds, When load, Then returns empty page`() =
        runTest {
            // Given
            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, 0, 10)
            } returns emptyList()

            // When
            val page = createPagingSource().loadPage(loadSize = 10)

            // Then
            assertEquals(0, page.data.size)
            assertNull(page.prevKey)
            assertNull(page.nextKey)
        }

    @Test
    fun `Given first page full results, When load, Then nextKey is offset plus limit`() =
        runTest {
            // Given
            val feeds = (1L..5L).map { mockFeedEntity(it) }
            val podcasts = (1L..5L).map { mockPodcastWithExtrasView(it) }

            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, 0, 5)
            } returns feeds
            coEvery { podcastLocal.getPodcastsByIdsOnce(any()) } returns podcasts

            // When
            val page = createPagingSource().loadPage(loadSize = 5)

            // Then
            assertEquals(5, page.data.size)
            assertNull(page.prevKey)
            assertEquals(5, page.nextKey)
        }

    @Test
    fun `Given last page partial, When load, Then nextKey null`() =
        runTest {
            // Given
            val feeds = (1L..2L).map { mockFeedEntity(it) }
            val podcasts = (1L..2L).map { mockPodcastWithExtrasView(it) }

            coEvery { feedLocal.getFeedsOldestCachedAt(defaultGroupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(defaultGroupKey, 10, 5)
            } returns feeds
            coEvery { podcastLocal.getPodcastsByIdsOnce(any()) } returns podcasts

            // When
            val page = createPagingSource().loadPage(key = 10, loadSize = 5)

            // Then
            assertEquals(2, page.data.size)
            assertEquals(5, page.prevKey)
            assertNull(page.nextKey)
        }

    @Test
    fun `Given exception during load, When load, Then returns LoadResult Error`() =
        runTest {
            // Given
            coEvery {
                feedLocal.getFeedsOldestCachedAt(any())
            } throws RuntimeException("test error")

            // When
            val result = createPagingSource().load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 10,
                    placeholdersEnabled = false
                )
            )

            // Then
            assertTrue(result is PagingSource.LoadResult.Error)
            val error = result as PagingSource.LoadResult.Error
            assertEquals("test error", error.throwable.message)
        }

    @Test
    fun `Given another groupKey, When load, Then reads and writes only that group`() =
        runTest {
            // Given
            val groupKey = "${GroupKey.TRENDING}:full:en:9"
            coEvery { feedLocal.getFeedsOldestCachedAt(groupKey) } returns Clock.System.now()
            coEvery {
                feedLocal.getFeedsPagingList(groupKey, 0, 10)
            } returns emptyList()

            // When
            val page = createPagingSource(groupKey = groupKey).loadPage(loadSize = 10)

            // Then
            assertEquals(0, page.data.size)
            coVerify { feedLocal.getFeedsOldestCachedAt(groupKey) }
            coVerify { feedLocal.getFeedsPagingList(groupKey, 0, 10) }
            coVerify(exactly = 0) { feedLocal.getFeedsPagingList(defaultGroupKey, any(), any()) }
        }
}
