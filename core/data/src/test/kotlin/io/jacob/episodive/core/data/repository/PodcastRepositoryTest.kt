package io.jacob.episodive.core.data.repository

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.jacob.episodive.core.data.util.query.PodcastQuery
import io.jacob.episodive.core.data.util.query.QueryScope
import io.jacob.episodive.core.data.util.updater.PodcastRemoteUpdater
import io.jacob.episodive.core.database.datasource.FeedLocalDataSource
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.mapper.toPodcastWithExtrasViews
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import io.jacob.episodive.core.network.model.PodcastResponse
import io.jacob.episodive.core.testing.model.podcastTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class PodcastRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val localDataSource = mockk<PodcastLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<PodcastRemoteDataSource>(relaxed = true)
    private val feedLocalDataSource = mockk<FeedLocalDataSource>(relaxed = true)
    private val feedRemoteDataSource = mockk<FeedRemoteDataSource>(relaxed = true)
    private val remoteUpdater = mockk<PodcastRemoteUpdater.Factory>(relaxed = true)

    private val repository: PodcastRepository = PodcastRepositoryImpl(
        podcastLocalDataSource = localDataSource,
        podcastRemoteDataSource = remoteDataSource,
        feedLocalDataSource = feedLocalDataSource,
        feedRemoteDataSource = feedRemoteDataSource,
        remoteUpdater = remoteUpdater,
    )

    private val podcastDtos = podcastTestDataList.toPodcastWithExtrasViews()

    @After
    fun teardown() {
        confirmVerified(
            localDataSource,
            remoteDataSource,
            feedLocalDataSource,
            feedRemoteDataSource,
            remoteUpdater,
        )
    }

    @Test
    fun `Given search, When searchPodcasts is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val search = "test"

            coEvery {
                remoteDataSource.searchPodcasts(any(), any())
            } returns listOf(mockk<PodcastResponse>(relaxed = true))

            // When
            repository.searchPodcasts(search, max = max).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                remoteDataSource.searchPodcasts(search, max)
            }
        }

    @Test
    fun `Given feedId, When getPodcastByFeedId is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val feedId = 12345L
            val query = PodcastQuery.FeedId(feedId)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(1) } returns flowOf(listOf(podcastDtos.first()))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getPodcastByFeedId(feedId).test {
                val result = awaitItem()
                // Then
                assertEquals(podcastTestData.id, result?.id)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(1)
            }
        }

    @Test
    fun `Given feedUrl, When getPodcastByFeedUrl is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val feedUrl = "test"
            val query = PodcastQuery.FeedUrl(feedUrl)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(1) } returns flowOf(listOf(podcastDtos.first()))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getPodcastByFeedUrl(feedUrl).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(1)
            }
        }

    @Test
    fun `Given guid, When getPodcastByGuid is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val guid = "test"
            val query = PodcastQuery.FeedGuid(guid)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(1) } returns flowOf(listOf(podcastDtos.first()))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getPodcastByGuid(guid).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(1)
            }
        }

    @Test
    fun `Given medium, When getPodcastsByMedium is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val medium = "test"
            val query = PodcastQuery.Medium(medium)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(max) } returns flowOf(podcastDtos)
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getPodcastsByMedium(medium, max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                assertEquals(podcastTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(max)
            }
        }

    @Test
    fun `Given dependencies, When getFollowedPodcasts is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val dtos = podcastDtos.mapIndexed { index, dto ->
                dto.copy(
                    followedAt = Instant.fromEpochSeconds(1757568578L + index),
                    isNotificationEnabled = true,
                )
            }
            coEvery { localDataSource.getFollowedPodcasts(limit = max) } returns flowOf(dtos)

            // When
            repository.getFollowedPodcasts(max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                assertEquals(podcastTestDataList[0].id, result[0].id)
                assertEquals(podcastTestDataList[1].id, result[1].id)
                awaitComplete()
            }

            // Then
            coVerifySequence {
                localDataSource.getFollowedPodcasts(limit = max)
            }
        }

    @Test
    fun `Given dependencies, When toggleFollowed is called, Then call methods of dataSources`() =
        runTest {
            // Given
            val podcastId = 12345L
            coEvery { localDataSource.toggleFollowedPodcast(podcastId) } returns true

            // When
            repository.toggleFollowed(podcastId)

            // Then
            coVerifySequence {
                localDataSource.toggleFollowedPodcast(podcastId)
            }
        }

    @Test
    fun `Given channel, When getPodcastsByChannel is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val channel = Channel(
                id = 1,
                title = "Test Channel",
                description = "Test Description",
                image = "https://example.com/image.jpg",
                link = "https://example.com",
                count = 3,
                podcastGuids = listOf("guid1", "guid2", "guid3")
            )
            val query = PodcastQuery.ByChannel(channel)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(100) } returns flowOf(podcastDtos)
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getPodcastsByChannel(channel).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                assertEquals(podcastTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(100)
            }
        }

    @Test
    fun `Given trending params, When getTrendingPodcasts is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val language = "en"
            val categories = listOf(Category.BUSINESS)
            val query = PodcastQuery.Trending(max, language, categories)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(max) } returns flowOf(podcastDtos)
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getTrendingPodcasts(max, language, categories).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                assertEquals(podcastTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(max)
            }
        }

    @Test
    fun `Given recent params, When getRecentPodcasts is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val language = "en"
            val categories = listOf(Category.COMEDY)
            val query = PodcastQuery.Recent(max, language, categories)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(max) } returns flowOf(podcastDtos)
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getRecentPodcasts(max, language, categories).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(max)
            }
        }

    @Test
    fun `Given max, When getRecentNewPodcasts is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val query = PodcastQuery.RecentNew(max)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(max) } returns flowOf(podcastDtos)
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getRecentNewPodcasts(max).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(max)
            }
        }

    @Test
    fun `Given recommended params, When getRecommendedPodcasts is called, Then calls methods of dataSources`() =
        runTest {
            // Given
            val max = 10
            val language = "en"
            val categories = listOf(Category.EDUCATION)
            val query = PodcastQuery.Recommended(max, language, categories)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery { updater.getFlowList(max) } returns flowOf(podcastDtos)
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            repository.getRecommendedPodcasts(max, language, categories).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(query)
                updater.getFlowList(max)
            }
        }

    @Test
    fun `Given medium, When getPodcastsByMediumPaging is called, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val medium = "podcast"
            val query = PodcastQuery.Medium(medium)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery {
                updater.getPagingData(any())
            } returns flowOf(PagingData.from(podcastDtos))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            val result = repository.getPodcastsByMediumPaging(medium).asSnapshot()

            // Then
            assertEquals(podcastTestDataList.size, result.size)
            assertEquals(podcastTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(query)
                updater.getPagingData(any())
            }
        }

    @Test
    fun `Given channel, When getPodcastsByChannelPaging is called, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val channel = Channel(
                id = 1,
                title = "Test Channel",
                description = "Test Description",
                image = "https://example.com/image.jpg",
                link = "https://example.com",
                count = 3,
                podcastGuids = listOf("guid1", "guid2", "guid3")
            )
            val query = PodcastQuery.ByChannel(channel)

            val updater = mockk<PodcastRemoteUpdater>(relaxed = true)
            coEvery {
                updater.getPagingData(any())
            } returns flowOf(PagingData.from(podcastDtos))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            val result = repository.getPodcastsByChannelPaging(channel).asSnapshot()

            // Then
            assertEquals(podcastTestDataList.size, result.size)
            assertEquals(podcastTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(query)
                updater.getPagingData(any())
            }
        }

    @Test
    fun `When getFollowedPodcastsPaging is called, Then returns flow of paging data`() =
        runTest {
            // Given
            coEvery { localDataSource.getFollowedPodcastsPaging(any()) } returns mockk(relaxed = true)

            // When
            val flow = repository.getFollowedPodcastsPaging()

            // Then
            assertNotNull(flow)
        }

    @Test
    fun `When getRecommendedPodcastsPaging is called, Then returns flow of paging data`() =
        runTest {
            // When
            val flow = repository.getRecommendedPodcastsPaging(max = 10)

            // Then
            assertNotNull(flow)
        }

    @Test
    fun `Given trending condition, When getTrendingPodcastsPaging is called, Then windows feeds under the full scope key`() =
        runTest {
            // 두 가지를 한꺼번에 못 박는다.
            // 하나, 그룹 키는 FULL 스코프여야 한다 — 미리보기와 같은 그룹을 쓰면 먼저 캐시를
            // 채운 쪽의 개수에 갇힌다.
            // 둘, 이 경로는 피드 목록만 받고 상세는 페이지 단위로 채운다. 업데이터로 되돌리면
            // 첫 화면이 목록 전체(1+50건)를 기다리게 되고, 아래 호출 순서가 어긋난다.
            // Given
            val query = PodcastQuery.Trending(
                max = 50,
                language = "ko",
                categories = emptyList(),
                scope = QueryScope.FULL,
            )
            coEvery { feedLocalDataSource.getFeedsOldestCachedAt(query.key) } returns null
            coEvery {
                feedRemoteDataSource.getTrendingFeeds(any(), any(), any(), any(), any())
            } returns emptyList()
            coEvery {
                feedLocalDataSource.getFeedsPagingList(query.key, any(), any())
            } returns emptyList()

            // When
            val result = repository.getTrendingPodcastsPaging(max = 50, language = "ko").asSnapshot()

            // Then
            assertEquals(emptyList<Podcast>(), result)

            coVerifySequence {
                feedLocalDataSource.getFeedsOldestCachedAt(query.key)
                feedRemoteDataSource.getTrendingFeeds(
                    max = 50,
                    since = null,
                    language = "ko",
                    includeCategories = "",
                    excludeCategories = null,
                )
                feedLocalDataSource.replaceFeedsByGroupKey(emptyList(), query.key)
                localDataSource.replacePodcasts(emptyList(), query.key)
                feedLocalDataSource.getFeedsPagingList(query.key, 0, 10)
            }
        }

    @Test
    fun `Given recent condition, When getRecentPodcastsPaging is called, Then windows feeds under the full scope key`() =
        runTest {
            // Given
            val query = PodcastQuery.Recent(
                max = 50,
                language = "ko",
                categories = emptyList(),
                scope = QueryScope.FULL,
            )
            coEvery { feedLocalDataSource.getFeedsOldestCachedAt(query.key) } returns null
            coEvery {
                feedRemoteDataSource.getRecentFeeds(any(), any(), any(), any(), any())
            } returns emptyList()
            coEvery {
                feedLocalDataSource.getFeedsPagingList(query.key, any(), any())
            } returns emptyList()

            // When
            val result = repository.getRecentPodcastsPaging(max = 50, language = "ko").asSnapshot()

            // Then
            assertEquals(emptyList<Podcast>(), result)

            coVerifySequence {
                feedLocalDataSource.getFeedsOldestCachedAt(query.key)
                feedRemoteDataSource.getRecentFeeds(
                    max = 50,
                    since = null,
                    language = "ko",
                    includeCategories = "",
                    excludeCategories = null,
                )
                feedLocalDataSource.replaceFeedsByGroupKey(emptyList(), query.key)
                localDataSource.replacePodcasts(emptyList(), query.key)
                feedLocalDataSource.getFeedsPagingList(query.key, 0, 10)
            }
        }

    @Test
    fun `Given same condition, When preview and full are requested, Then queries differ`() =
        runTest {
            // 비페이징 경로는 기본값인 PREVIEW 를 써야 한다. 이게 FULL 로 새면 홈이 전체
            // 목록 캐시를 덮어쓴다.
            // Given & When
            val preview = PodcastQuery.Trending(max = 10, language = "ko")
            val full = PodcastQuery.Trending(max = 50, language = "ko", scope = QueryScope.FULL)

            // Then
            assertNotEquals(preview.key, full.key)
        }
}