package io.jacob.episodive.core.data.util.updater

import androidx.paging.PagingConfig
import app.cash.turbine.test
import io.jacob.episodive.core.data.util.query.EpisodeQuery
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.SoundbiteLocalDataSource
import io.jacob.episodive.core.database.model.EpisodeWithExtrasView
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.DataErrorException
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.datasource.SoundbiteRemoteDataSource
import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class EpisodeRemoteUpdaterTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodeLocal = mockk<EpisodeLocalDataSource>(relaxed = true)
    private val episodeRemote = mockk<EpisodeRemoteDataSource>(relaxed = true)
    private val soundbiteLocal = mockk<SoundbiteLocalDataSource>(relaxed = true)
    private val soundbiteRemote = mockk<SoundbiteRemoteDataSource>(relaxed = true)

    @After
    fun teardown() {
        confirmVerified(
            episodeLocal,
            episodeRemote,
            soundbiteLocal,
            soundbiteRemote,
        )
    }

    @Test
    fun `Given dependencies, When person query, Then call dataSource's functions`() =
        runTest {
            // Given
            val person = "John Doe"
            val query = EpisodeQuery.Person(person)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.searchEpisodesByPerson(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.searchEpisodesByPerson(person, 1000)
                episodeLocal.replaceEpisodes(any(), any())
            }
        }

    @Test
    fun `Given dependencies, When person query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val person = "John Doe"
            val query = EpisodeQuery.Person(person)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.searchEpisodesByPerson(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
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
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.searchEpisodesByPerson(person, 1000)
                episodeLocal.replaceEpisodes(any(), any())
                episodeLocal.getEpisodesByGroupKeyPaging(any())
            }
        }

    @Test
    fun `Given dependencies, When feedId query, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedId = 123L
            val query = EpisodeQuery.FeedId(feedId)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByFeedId(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByFeedId(feedId, 1000)
                episodeLocal.replaceEpisodes(any(), any())
            }
        }

    @Test
    fun `Given dependencies, When feedId query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedId = 123L
            val query = EpisodeQuery.FeedId(feedId)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByFeedId(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
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
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByFeedId(feedId, 1000)
                episodeLocal.replaceEpisodes(any(), any())
                episodeLocal.getEpisodesByGroupKeyPaging(any())
            }
        }

    @Test
    fun `Given cached episodes and expired cache, When remote fetch fails, Then emit cached episodes without replacing`() =
        runTest {
            // Given
            val feedId = 123L
            val query = EpisodeQuery.FeedId(feedId)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            val cachedEpisodes = listOf(mockk<EpisodeWithExtrasView>(relaxed = true))
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns flowOf(cachedEpisodes)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns Clock.System.now() - 2.days
            coEvery {
                episodeRemote.getEpisodesByFeedId(any(), any())
            } throws RuntimeException("network error")

            // When & Then
            // 원격 갱신이 실패해도 Flow 가 끊기지 않고 캐시가 그대로 방출돼야 한다.
            updater.getFlowList(count = 10).test {
                assertEquals(cachedEpisodes, awaitItem())
                awaitComplete()
            }

            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByFeedId(feedId, 1000)
            }
        }

    @Test
    fun `Given no cached episodes, When remote fetch fails, Then propagate the error`() =
        runTest {
            // Given
            val feedId = 123L
            val query = EpisodeQuery.FeedId(feedId)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            // SocketTimeoutException 을 던져 toDataError() 판별이 실제로 동작하는지까지 고정한다.
            val error = SocketTimeoutException("network error")
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns flowOf(emptyList())
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByFeedId(any(), any())
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
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByFeedId(feedId, 1000)
            }
        }

    @Test
    fun `Given cached episodes within TTL, When getFlowList called, Then skip remote fetch`() =
        runTest {
            // Given
            val feedId = 123L
            val query = EpisodeQuery.FeedId(feedId)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            val cachedEpisodes = listOf(mockk<EpisodeWithExtrasView>(relaxed = true))
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns flowOf(cachedEpisodes)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns Clock.System.now()

            // When
            updater.getFlowList(count = 10).test {
                assertEquals(cachedEpisodes, awaitItem())
                awaitComplete()
            }

            // Then: TTL 이내라 원격 갱신을 아예 호출하지 않는다.
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            }
        }

    @Test
    fun `Given dependencies, When feedUrl query, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedUrl = "https://example.com/feed.xml"
            val query = EpisodeQuery.FeedUrl(feedUrl)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByFeedUrl(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByFeedUrl(feedUrl, 1000)
                episodeLocal.replaceEpisodes(any(), any())
            }
        }

    @Test
    fun `Given dependencies, When feedUrl query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val feedUrl = "https://example.com/feed.xml"
            val query = EpisodeQuery.FeedUrl(feedUrl)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByFeedUrl(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
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
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByFeedUrl(feedUrl, 1000)
                episodeLocal.replaceEpisodes(any(), any())
                episodeLocal.getEpisodesByGroupKeyPaging(any())
            }
        }

    @Test
    fun `Given dependencies, When podcastGuid query, Then call dataSource's functions`() =
        runTest {
            // Given
            val podcastGuid = "test-guid"
            val query = EpisodeQuery.PodcastGuid(podcastGuid)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByPodcastGuid(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = 10).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), 10)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByPodcastGuid(podcastGuid, 1000)
                episodeLocal.replaceEpisodes(any(), any())
            }
        }

    @Test
    fun `Given dependencies, When podcastGuid query paging, Then call dataSource's functions`() =
        runTest {
            // Given
            val podcastGuid = "test-guid"
            val query = EpisodeQuery.PodcastGuid(podcastGuid)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKeyPaging(any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getEpisodesByPodcastGuid(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
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
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getEpisodesByPodcastGuid(podcastGuid, 1000)
                episodeLocal.replaceEpisodes(any(), any())
                episodeLocal.getEpisodesByGroupKeyPaging(any())
            }
        }

    @Test
    fun `Given dependencies, When live query, Then call dataSource's functions`() =
        runTest {
            // Given
            val max = 10
            val query = EpisodeQuery.Live(max = max)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getLiveEpisodes(any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = max).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), max)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getLiveEpisodes(max = max)
                episodeLocal.replaceEpisodes(any(), any())
            }
        }

    @Test
    fun `Given dependencies, When random query, Then call dataSource's functions`() =
        runTest {
            // Given
            val max = 10
            val query = EpisodeQuery.Random(max = max)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getRandomEpisodes(any(), any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = max).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), max)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getRandomEpisodes(max = max, any(), any())
                episodeLocal.replaceEpisodes(any(), any())
            }
        }

    @Test
    fun `Given dependencies, When recent query, Then call dataSource's functions`() =
        runTest {
            // Given
            val max = 10
            val query = EpisodeQuery.Recent(max = max)
            val updater = EpisodeRemoteUpdater(
                episodeLocal = episodeLocal,
                episodeRemote = episodeRemote,
                soundbiteLocal = soundbiteLocal,
                soundbiteRemote = soundbiteRemote,
                query = query,
            )
            coEvery {
                episodeLocal.getEpisodesByGroupKey(any(), any())
            } returns mockk(relaxed = true)
            coEvery {
                episodeLocal.getOldestCreatedAtByGroupKey(any())
            } returns null
            coEvery {
                episodeRemote.getRecentEpisodes(any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))
            coEvery {
                episodeLocal.replaceEpisodes(any(), any())
            } just Runs

            // When
            updater.getFlowList(count = max).test {
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerifySequence {
                episodeLocal.getEpisodesByGroupKey(any(), max)
                episodeLocal.getOldestCreatedAtByGroupKey(any())
                episodeRemote.getRecentEpisodes(max = max)
                episodeLocal.replaceEpisodes(any(), any())
            }
        }
}