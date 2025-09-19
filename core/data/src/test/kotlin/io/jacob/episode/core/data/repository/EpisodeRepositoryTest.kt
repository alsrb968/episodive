package io.jacob.episode.core.data.repository

import app.cash.turbine.test
import io.jacob.episodive.core.data.model.EpisodeQuery
import io.jacob.episodive.core.data.repository.EpisodeRepositoryImpl
import io.jacob.episodive.core.data.util.Cacher
import io.jacob.episodive.core.data.util.EpisodeRemoteUpdater
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.testing.data.episodeTestData
import io.jacob.episodive.core.testing.data.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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
                assertEquals(10, result.size)
                assertEquals(episodeTestDataList, result)
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
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
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
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
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
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
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
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
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
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                localDataSource.getEpisodesByCacheKey(expectedQuery.key)
            }
        }
}