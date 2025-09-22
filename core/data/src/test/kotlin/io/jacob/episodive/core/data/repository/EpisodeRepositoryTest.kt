package io.jacob.episodive.core.data.repository

import app.cash.turbine.test
import io.jacob.episodive.core.data.util.EpisodeQuery
import io.jacob.episodive.core.data.util.EpisodeRemoteUpdater
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class EpisodeRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val localDataSource = mockk<EpisodeLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<EpisodeRemoteDataSource>(relaxed = true)
    private val remoteUpdater = mockk<EpisodeRemoteUpdater.Factory>(relaxed = true)

    private val repository: EpisodeRepository = EpisodeRepositoryImpl(
        localDataSource = localDataSource,
        remoteDataSource = remoteDataSource,
        remoteUpdater = remoteUpdater,
    )

    private val episodeEntities = episodeTestDataList.toEpisodeEntities("test_key")

    @After
    fun teardown() {
        confirmVerified(localDataSource, remoteDataSource, remoteUpdater)
    }

    @Test
    fun `Given person, When searchEpisodesByPerson, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val person = "John Doe"
            val expectedQuery = EpisodeQuery.Person(person)

            coEvery {
                remoteUpdater.create(expectedQuery)
            } returns mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            } returns flowOf(episodeEntities)

            // When
            repository.searchEpisodesByPerson(person, max = 10).test {
                val result = awaitItem()
                // Then
                Assert.assertEquals(10, result.size)
                Assert.assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }

    @Test
    fun `Given feedId, When getEpisodesByFeedId, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val feedId = 123L
            val expectedQuery = EpisodeQuery.FeedId(feedId)

            coEvery {
                remoteUpdater.create(expectedQuery)
            } returns mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            } returns flowOf(episodeEntities)

            // When
            repository.getEpisodesByFeedId(feedId, max = 10).test {
                val result = awaitItem()
                // Then
                Assert.assertEquals(episodeTestDataList.size, result.size)
                Assert.assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }

    @Test
    fun `Given feedUrl, When getEpisodesByFeedUrl, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val feedUrl = "https://example.com/feed.xml"
            val expectedQuery = EpisodeQuery.FeedUrl(feedUrl)

            coEvery {
                remoteUpdater.create(expectedQuery)
            } returns mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            } returns flowOf(episodeEntities)

            // When
            repository.getEpisodesByFeedUrl(feedUrl, max = 10).test {
                val result = awaitItem()
                // Then
                Assert.assertEquals(episodeTestDataList.size, result.size)
                Assert.assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }

    @Test
    fun `Given podcastGuid, When getEpisodesByPodcastGuid, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val guid = "test-podcast-guid"
            val expectedQuery = EpisodeQuery.PodcastGuid(guid)

            coEvery {
                remoteUpdater.create(expectedQuery)
            } returns mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            } returns flowOf(episodeEntities)

            // When
            repository.getEpisodesByPodcastGuid(guid, max = 10).test {
                val result = awaitItem()
                // Then
                Assert.assertEquals(episodeTestDataList.size, result.size)
                Assert.assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }

    @Test
    fun `Given episodeId, When getEpisodeById, Then calls remoteDataSource directly`() =
        runTest {
            // Given
            coEvery { remoteDataSource.getEpisodeById(any()) } returns mockk(relaxed = true)

            // When
            repository.getEpisodeById(123).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerify { remoteDataSource.getEpisodeById(any()) }
        }

    @Test
    fun `When getLiveEpisodes, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val expectedQuery = EpisodeQuery.Live

            coEvery {
                remoteUpdater.create(expectedQuery)
            } returns mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            } returns flowOf(episodeEntities)

            // When
            repository.getLiveEpisodes(max = 10).test {
                val result = awaitItem()
                // Then
                Assert.assertEquals(episodeTestDataList.size, result.size)
                Assert.assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }

    @Test
    fun `Given parameters, When getRandomEpisodes, Then calls remoteDataSource directly`() =
        runTest {
            // Given
            coEvery {
                remoteDataSource.getRandomEpisodes(any(), any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            repository.getRandomEpisodes().test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerify {
                remoteDataSource.getRandomEpisodes(any(), any(), any(), any())
            }
        }

    @Test
    fun `When getRecentEpisodes, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val expectedQuery = EpisodeQuery.Recent

            coEvery {
                remoteUpdater.create(expectedQuery)
            } returns mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            } returns flowOf(episodeEntities)

            // When
            repository.getRecentEpisodes(max = 10).test {
                val result = awaitItem()
                // Then
                Assert.assertEquals(episodeTestDataList.size, result.size)
                Assert.assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }

    @Test
    fun `When getLikedEpisodes, Then calls localDataSource directly`() =
        runTest {
            // Given
            coEvery { localDataSource.getLikedEpisodes() } returns flowOf(mockk(relaxed = true))

            // When
            repository.getLikedEpisodes().test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerify { localDataSource.getLikedEpisodes() }
        }

    @Test
    fun `When getPlayingEpisodes, Then calls localDataSource directly`() =
        runTest {
            // Given
            coEvery { localDataSource.getPlayingEpisodes() } returns flowOf(mockk(relaxed = true))

            // When
            repository.getPlayingEpisodes().test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerify { localDataSource.getPlayingEpisodes() }
        }

    @Test
    fun `When getPlayedEpisodes, Then calls localDataSource directly`() =
        runTest {
            // Given
            coEvery { localDataSource.getPlayedEpisodes() } returns flowOf(mockk(relaxed = true))

            // When
            repository.getPlayedEpisodes().test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerify { localDataSource.getPlayedEpisodes() }
        }

    @Test
    fun `Given episode is liked, When toggleLiked, Then removes liked and returns false`() =
        runTest {
            // Given
            val episodeId = 123L
            coEvery { localDataSource.isLiked(episodeId) } returns flowOf(true)
            coEvery { localDataSource.removeLiked(episodeId) } returns Unit

            // When
            val result = repository.toggleLiked(episodeId)

            // Then
            Assert.assertFalse(result)
            coVerifySequence {
                localDataSource.isLiked(episodeId)
                localDataSource.removeLiked(episodeId)
            }
        }

    @Test
    fun `Given episode is not liked, When toggleLiked, Then adds liked and returns true`() =
        runTest {
            // Given
            val episodeId = 123L
            coEvery { localDataSource.isLiked(episodeId) } returns flowOf(false)
            coEvery { localDataSource.addLiked(any()) } returns Unit

            // When
            val result = repository.toggleLiked(episodeId)

            // Then
            Assert.assertTrue(result)
            coVerifySequence {
                localDataSource.isLiked(episodeId)
                localDataSource.addLiked(any())
            }
        }

    @Test
    fun `When updatePlayed, Then calls localDataSource upsertPlayed`() =
        runTest {
            // Given
            val episodeId = 123L
            val position = 30.seconds
            val isCompleted = false
            coEvery { localDataSource.upsertPlayed(any()) } returns Unit

            // When
            repository.updatePlayed(episodeId, position, isCompleted)

            // Then
            coVerify {
                localDataSource.upsertPlayed(
                    match {
                        it.id == episodeId && it.position == position && it.isCompleted == isCompleted
                    }
                )
            }
        }
}