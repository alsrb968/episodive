package io.jacob.episodive.core.data.util.updater

import androidx.paging.PagingConfig
import app.cash.turbine.test
import io.jacob.episodive.core.data.util.query.PodcastQuery
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.model.PodcastWithExtrasView
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.DataErrorException
import io.jacob.episodive.core.model.Medium
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.model.PodcastResponse
import io.jacob.episodive.core.network.model.RecentFeedResponse
import io.jacob.episodive.core.network.model.RecentNewFeedResponse
import io.jacob.episodive.core.network.model.TrendingFeedResponse
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class PodcastRemoteUpdaterTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 백그라운드 갱신을 테스트 디스패처에서 돌린다. UnconfinedTestDispatcher 라 launch 가
    // 곧바로 실행돼, 검증 시점에는 갱신이 이미 끝나 있다.
    private val backgroundRefresher =
        BackgroundRefresher(CoroutineScope(mainDispatcherRule.testDispatcher))

    private val podcastLocal = mockk<PodcastLocalDataSource>(relaxed = true)
    private val podcastRemote = mockk<PodcastRemoteDataSource>(relaxed = true)
    private val feedRemote = mockk<FeedRemoteDataSource>(relaxed = true)

    @After
    fun teardown() {
        confirmVerified(
            podcastLocal,
            podcastRemote,
            feedRemote,
        )
    }

    @Test
    fun `Given dependencies, When feedId query, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedId = 123L
            val query = PodcastQuery.FeedId(feedId)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("feedId:123", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("feedId:123")
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "feedId:123")
            }
        }

    @Test
    fun `Given cached podcasts and expired cache, When remote fetch fails, Then emit cached podcasts without replacing`() =
        runTest {
            // Given
            val feedId = 123L
            val query = PodcastQuery.FeedId(feedId)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            val cachedPodcasts = listOf(mockk<PodcastWithExtrasView>(relaxed = true))
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns flowOf(cachedPodcasts)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns Clock.System.now() - 2.hours
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } throws RuntimeException("network error")

            // When & Then
            // 원격 갱신이 실패해도 Flow 가 끊기지 않고 캐시가 그대로 방출돼야 한다.
            updater.getFlowList(count = 10).test {
                assertEquals(cachedPodcasts, awaitItem())
                awaitComplete()
            }

            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("feedId:123", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("feedId:123")
                podcastRemote.getPodcastByFeedId(any())
            }
        }

    @Test
    fun `Given no cached podcasts, When remote fetch fails, Then propagate the error`() =
        runTest {
            // Given
            val feedId = 123L
            val query = PodcastQuery.FeedId(feedId)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            // SocketTimeoutException 을 던져 toDataError() 판별이 실제로 동작하는지까지 고정한다.
            val error = SocketTimeoutException("network error")
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns flowOf(emptyList())
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } throws error

            // When & Then
            // 보여줄 캐시가 없으면 예외를 삼키지 않고, 판별된 DataError 와 원인을 실어 올린다.
            updater.getFlowList(count = 10).test {
                val thrown = awaitError()
                assertTrue(thrown is DataErrorException)
                assertEquals(error, (thrown as DataErrorException).cause)
                assertEquals(DataError.Timeout, thrown.error)
            }

            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("feedId:123", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("feedId:123")
                podcastRemote.getPodcastByFeedId(any())
            }
        }

    @Test
    fun `Given cached podcasts within TTL, When getFlowList called, Then skip remote fetch`() =
        runTest {
            // Given
            val feedId = 123L
            val query = PodcastQuery.FeedId(feedId)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            val cachedPodcasts = listOf(mockk<PodcastWithExtrasView>(relaxed = true))
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns flowOf(cachedPodcasts)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns Clock.System.now()

            // When
            updater.getFlowList(count = 10).test {
                assertEquals(cachedPodcasts, awaitItem())
                awaitComplete()
            }

            // Then: TTL 이내라 원격 갱신을 아예 호출하지 않는다.
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("feedId:123", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("feedId:123")
            }
        }

    @Test
    fun `Given dependencies, When feedId query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedId = 123L
            val query = PodcastQuery.FeedId(feedId)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("feedId:123")
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "feedId:123")
                podcastLocal.getPodcastsByGroupKeyPaging("feedId:123")
            }
        }

    @Test
    fun `Given dependencies, When feedUrl query, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedUrl = "test-url"
            val query = PodcastQuery.FeedUrl(feedUrl)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByFeedUrl(any())
            } returns mockk<PodcastResponse>(relaxed = true)
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("feedUrl:test-url", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("feedUrl:test-url")
                podcastRemote.getPodcastByFeedUrl(any())
                podcastLocal.replacePodcasts(any(), "feedUrl:test-url")
            }
        }

    @Test
    fun `Given dependencies, When feedUrl query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedUrl = "test-url"
            val query = PodcastQuery.FeedUrl(feedUrl)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByFeedUrl(any())
            } returns mockk<PodcastResponse>(relaxed = true)
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("feedUrl:test-url")
                podcastRemote.getPodcastByFeedUrl(any())
                podcastLocal.replacePodcasts(any(), "feedUrl:test-url")
                podcastLocal.getPodcastsByGroupKeyPaging("feedUrl:test-url")
            }
        }

    @Test
    fun `Given dependencies, When feedGuid query, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedGuid = "test-guid"
            val query = PodcastQuery.FeedGuid(feedGuid)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByGuid(any())
            } returns mockk<PodcastResponse>(relaxed = true)
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("feedGuid:test-guid", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("feedGuid:test-guid")
                podcastRemote.getPodcastByGuid(any())
                podcastLocal.replacePodcasts(any(), "feedGuid:test-guid")
            }
        }

    @Test
    fun `Given dependencies, When feedGuid query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedGuid = "test-guid"
            val query = PodcastQuery.FeedGuid(feedGuid)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastByGuid(any())
            } returns mockk<PodcastResponse>(relaxed = true)
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("feedGuid:test-guid")
                podcastRemote.getPodcastByGuid(any())
                podcastLocal.replacePodcasts(any(), "feedGuid:test-guid")
                podcastLocal.getPodcastsByGroupKeyPaging("feedGuid:test-guid")
            }
        }

    @Test
    fun `Given dependencies, When medium query, Then call dataSource's functions`() =
        runTest {
            // Given
            val medium = Medium.PODCAST.value
            val query = PodcastQuery.Medium(medium)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastsByMedium(any(), any())
            } returns listOf(mockk<PodcastResponse>(relaxed = true))
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("medium:podcast", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("medium:podcast")
                podcastRemote.getPodcastsByMedium(any(), 1000)
                podcastLocal.replacePodcasts(any(), "medium:podcast")
            }
        }

    @Test
    fun `Given dependencies, When medium query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val medium = Medium.PODCAST.value
            val query = PodcastQuery.Medium(medium)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastsByMedium(any(), any())
            } returns listOf(mockk<PodcastResponse>(relaxed = true))
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("medium:podcast")
                podcastRemote.getPodcastsByMedium(any(), 1000)
                podcastLocal.replacePodcasts(any(), "medium:podcast")
                podcastLocal.getPodcastsByGroupKeyPaging("medium:podcast")
            }
        }

    @Test
    fun `Given dependencies, When channel query, Then call dataSource's functions`() =
        runTest {
            // Given
            val channel = Channel(
                id = 1,
                title = "test",
                description = "test",
                image = "test",
                link = "test",
                count = 1,
                podcastGuids = listOf("test")
            )
            val query = PodcastQuery.ByChannel(channel)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastsByGuids(any())
            } returns listOf(mockk<PodcastResponse>(relaxed = true))
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("channel:1", 10)
                podcastLocal.getOldestCreatedAtByGroupKey("channel:1")
                podcastRemote.getPodcastsByGuids(any())
                podcastLocal.replacePodcasts(any(), "channel:1")
            }
        }

    @Test
    fun `Given dependencies, When channel query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val channel = Channel(
                id = 1,
                title = "test",
                description = "test",
                image = "test",
                link = "test",
                count = 1,
                podcastGuids = listOf("test")
            )
            val query = PodcastQuery.ByChannel(channel)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastRemote.getPodcastsByGuids(any())
            } returns listOf(mockk<PodcastResponse>(relaxed = true))
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("channel:1")
                podcastRemote.getPodcastsByGuids(any())
                podcastLocal.replacePodcasts(any(), "channel:1")
                podcastLocal.getPodcastsByGroupKeyPaging("channel:1")
            }
        }

    @Test
    fun `Given dependencies, When trending query, Then call dataSources's functions`() =
        runTest {
            // Given
            val max = 100
            val query = PodcastQuery.Trending(
                max = max,
                language = "ko",
                categories = listOf(Category.BUSINESS, Category.POLITICS),
            )
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs
            coEvery {
                feedRemote.getTrendingFeeds(any(), any(), any(), any(), any())
            } returns listOf(mockk<TrendingFeedResponse>(relaxed = true))
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)

            // When
            updater.getFlowList(count = max).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("trending:preview:ko:9,59", max)
                podcastLocal.getOldestCreatedAtByGroupKey("trending:preview:ko:9,59")
                feedRemote.getTrendingFeeds(
                    max = max,
                    since = any(),
                    language = "ko",
                    includeCategories = "9,59",
                    excludeCategories = any(),
                )
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "trending:preview:ko:9,59")
            }
        }

    @Test
    fun `Given dependencies, When trending query paging, Then call dataSources's functions`() =
        runTest {
            // Given
            val max = 10
            val query = PodcastQuery.Trending(
                max = max,
                language = "ko",
                categories = listOf(Category.BUSINESS, Category.POLITICS),
            )
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs
            coEvery {
                feedRemote.getTrendingFeeds(any(), any(), any(), any(), any())
            } returns listOf(mockk<TrendingFeedResponse>(relaxed = true))
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("trending:preview:ko:9,59")
                feedRemote.getTrendingFeeds(
                    max = max,
                    since = any(),
                    language = "ko",
                    includeCategories = "9,59",
                    excludeCategories = any(),
                )
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "trending:preview:ko:9,59")
                podcastLocal.getPodcastsByGroupKeyPaging("trending:preview:ko:9,59")
            }
        }

    @Test
    fun `Given dependencies, When recent query, Then call dataSources's functions`() =
        runTest {
            // Given
            val max = 10
            val query = PodcastQuery.Recent(
                max = max,
                language = "ko",
                categories = listOf(Category.BUSINESS, Category.POLITICS),
            )
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs
            coEvery {
                feedRemote.getRecentFeeds(any(), any(), any(), any(), any())
            } returns listOf(mockk<RecentFeedResponse>(relaxed = true))
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)

            // When
            updater.getFlowList(count = max).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("recent:preview:ko:9,59", max)
                podcastLocal.getOldestCreatedAtByGroupKey("recent:preview:ko:9,59")
                feedRemote.getRecentFeeds(
                    max = max,
                    since = any(),
                    language = "ko",
                    includeCategories = "9,59",
                    excludeCategories = any(),
                )
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "recent:preview:ko:9,59")
            }
        }

    @Test
    fun `Given dependencies, When recent query paging, Then call dataSources's functions`() =
        runTest {
            // Given
            val max = 10
            val query = PodcastQuery.Recent(
                max = max,
                language = "ko",
                categories = listOf(Category.BUSINESS, Category.POLITICS),
            )
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs
            coEvery {
                feedRemote.getRecentFeeds(any(), any(), any(), any(), any())
            } returns listOf(mockk<RecentFeedResponse>(relaxed = true))
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("recent:preview:ko:9,59")
                feedRemote.getRecentFeeds(
                    max = max,
                    since = any(),
                    language = "ko",
                    includeCategories = "9,59",
                    excludeCategories = any(),
                )
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "recent:preview:ko:9,59")
                podcastLocal.getPodcastsByGroupKeyPaging("recent:preview:ko:9,59")
            }
        }

    @Test
    fun `Given dependencies, When recent new query, Then call dataSources's functions`() =
        runTest {
            // Given
            val max = 10
            val query = PodcastQuery.RecentNew(max = max)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs
            coEvery {
                feedRemote.getRecentNewFeeds(any(), any())
            } returns listOf(mockk<RecentNewFeedResponse>(relaxed = true))
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)

            // When
            updater.getFlowList(count = max).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getPodcastsByGroupKey("recentNew", max)
                podcastLocal.getOldestCreatedAtByGroupKey("recentNew")
                feedRemote.getRecentNewFeeds(
                    max = max,
                    since = any(),
                )
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "recentNew")
            }
        }

    @Test
    fun `Given dependencies, When recent new query paging, Then call dataSources's functions`() =
        runTest {
            // Given
            val max = 10
            val query = PodcastQuery.RecentNew(max = max)
            val updater = PodcastRemoteUpdater(
                podcastLocal = podcastLocal,
                podcastRemote = podcastRemote,
                feedRemote = feedRemote,
                query = query,
                backgroundRefresher = backgroundRefresher,
            )
            coEvery {
                podcastLocal.getPodcastsByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                podcastLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                podcastLocal.replacePodcasts(any(), any())
            } just Runs
            coEvery {
                feedRemote.getRecentNewFeeds(any(), any())
            } returns listOf(mockk<RecentNewFeedResponse>(relaxed = true))
            coEvery {
                podcastRemote.getPodcastByFeedId(any())
            } returns mockk<PodcastResponse>(relaxed = true)

            // When
            updater.getPagingData(
                pagingConfig = PagingConfig(
                    pageSize = 10,
                    initialLoadSize = 10,
                    prefetchDistance = 5,
                )
            ).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                podcastLocal.getOldestCreatedAtByGroupKey("recentNew")
                feedRemote.getRecentNewFeeds(
                    max = max,
                    since = any(),
                )
                podcastRemote.getPodcastByFeedId(any())
                podcastLocal.replacePodcasts(any(), "recentNew")
                podcastLocal.getPodcastsByGroupKeyPaging("recentNew")
            }
        }
}