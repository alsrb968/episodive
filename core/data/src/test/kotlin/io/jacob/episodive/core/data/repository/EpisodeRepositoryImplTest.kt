package io.jacob.episodive.core.data.repository

import app.cash.turbine.test
import io.jacob.episodive.core.data.util.updater.EpisodeRemoteUpdater
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.SoundbiteLocalDataSource
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.network.datasource.ChapterRemoteDataSource
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.datasource.SoundbiteRemoteDataSource
import io.jacob.episodive.core.network.mapper.toEpisodeResponses
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class EpisodeRepositoryImplTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodeLocalDataSource = mockk<EpisodeLocalDataSource>(relaxed = true)
    private val episodeRemoteDataSource = mockk<EpisodeRemoteDataSource>(relaxed = true)
    private val chapterRemoteDataSource = mockk<ChapterRemoteDataSource>(relaxed = true)
    private val soundbiteLocalDataSource = mockk<SoundbiteLocalDataSource>(relaxed = true)
    private val soundbiteRemoteDataSource = mockk<SoundbiteRemoteDataSource>(relaxed = true)
    private val remoteUpdater = mockk<EpisodeRemoteUpdater.Factory>(relaxed = true)

    private val repository = EpisodeRepositoryImpl(
        episodeLocalDataSource = episodeLocalDataSource,
        episodeRemoteDataSource = episodeRemoteDataSource,
        chapterRemoteDataSource = chapterRemoteDataSource,
        soundbiteLocalDataSource = soundbiteLocalDataSource,
        soundbiteRemoteDataSource = soundbiteRemoteDataSource,
        remoteUpdater = remoteUpdater,
    )

    // --- 딥링크 착지용 원격 폴백 ---

    @Test
    fun `Given a remote hit, When fetchEpisodeById, Then it returns the episode`() = runTest {
        val episode = episodeTestData
        coEvery {
            episodeRemoteDataSource.getEpisodeById(episode.id, fulltext = true)
        } returns listOf(episode).toEpisodeResponses().first()

        assertEquals(episode.id, repository.fetchEpisodeById(episode.id)?.id)
    }

    @Test
    fun `Given the remote has no such episode, When fetchEpisodeById, Then it returns null`() =
        runTest {
            coEvery { episodeRemoteDataSource.getEpisodeById(any(), any()) } returns null

            assertNull(repository.fetchEpisodeById(999L))
        }

    @Test
    fun `Given the remote call fails, When fetchEpisodeById, Then it returns null instead of throwing`() =
        runTest {
            // 호출부는 "로컬에 없으면 원격도 본다"는 폴백 자리라, 없는 것과 못 가져온 것을
            // 똑같이 다룬다. 여기서 던지면 플레이어가 그 예외를 다시 받아 처리해야 한다.
            coEvery { episodeRemoteDataSource.getEpisodeById(any(), any()) } throws
                    RuntimeException("boom")

            assertNull(repository.fetchEpisodeById(999L))
        }

    @Test
    fun `Given a cancellation, When fetchEpisodeById, Then it propagates`() = runTest {
        // 취소는 삼키면 안 된다 — 코루틴 취소가 전파되지 않으면 화면을 떠난 뒤에도 일이 남는다.
        coEvery { episodeRemoteDataSource.getEpisodeById(any(), any()) } throws
                CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.fetchEpisodeById(999L) }
        }
    }

    @Test
    fun `Given feedId, When getLatestEpisodeDatePublished, Then delegates to localDataSource`() =
        runTest {
            // Given
            val feedId = 5778530L
            val expected = Instant.fromEpochSeconds(1000)
            coEvery { episodeLocalDataSource.getLatestEpisodeDatePublished(feedId) } returns expected

            // When
            val result = repository.getLatestEpisodeDatePublished(feedId)

            // Then
            assertEquals(expected, result)
            coVerify { episodeLocalDataSource.getLatestEpisodeDatePublished(feedId) }
        }

    @Test
    fun `Given feedId and since, When fetchAndSaveNewEpisodes, Then fetches from remote and saves locally`() =
        runTest {
            // Given
            val feedId = 5778530L
            val since = Instant.fromEpochSeconds(1000)
            coEvery {
                episodeRemoteDataSource.getEpisodesByFeedId(feedId = feedId, since = since.epochSeconds)
            } returns emptyList()

            // When
            val result = repository.fetchAndSaveNewEpisodes(feedId, since)

            // Then
            assertEquals(emptyList<Any>(), result)
            coVerify { episodeRemoteDataSource.getEpisodesByFeedId(feedId = feedId, since = since.epochSeconds) }
            coVerify { episodeLocalDataSource.upsertEpisodes(emptyList()) }
        }

    @Test
    fun `Given boundary and newer episodes, When fetchAndSaveNewEpisodes, Then returns only episodes after since`() =
        runTest {
            // Given
            val feedId = 5778530L
            val since = Instant.fromEpochSeconds(1000)
            val boundaryEpisode = episodeTestData.copy(id = 1L, datePublished = since)
            val newerEpisode = episodeTestData.copy(id = 2L, datePublished = Instant.fromEpochSeconds(2000))
            coEvery {
                episodeRemoteDataSource.getEpisodesByFeedId(feedId = feedId, since = since.epochSeconds)
            } returns listOf(boundaryEpisode, newerEpisode).toEpisodeResponses()

            // When
            val result = repository.fetchAndSaveNewEpisodes(feedId, since)

            // Then: 이미 보유한 경계 에피소드는 제외하고 since 이후 새 에피소드만 반환
            assertEquals(1, result.size)
            assertEquals(2L, result[0].id)
            // DB 캐시에는 응답 전체를 저장한다
            coVerify { episodeLocalDataSource.upsertEpisodes(any()) }
        }

    @Test
    fun `Given episode, When upsertEpisode, Then delegates to localDataSource`() = runTest {
        // Given
        val episode = episodeTestData

        // When
        repository.upsertEpisode(episode)

        // Then
        coVerify { episodeLocalDataSource.upsertEpisode(any()) }
    }

    @Test
    fun `Given episodeId, When getEpisodeById with no episode, Then returns null`() = runTest {
        // Given
        val id = 999L
        every { episodeLocalDataSource.getEpisodeById(id) } returns flowOf(null)

        // When / Then
        repository.getEpisodeById(id).test {
            assertNull(awaitItem())
            awaitComplete()
        }
        coVerify { episodeLocalDataSource.getEpisodeById(id) }
    }

    @Test
    fun `Given episodes and groupKey, When replaceEpisodes, Then delegates to localDataSource`() =
        runTest {
            // Given
            val episodes = emptyList<Episode>()
            val groupKey = "feedId:123"

            // When
            repository.replaceEpisodes(episodes, groupKey)

            // Then
            coVerify { episodeLocalDataSource.replaceEpisodes(emptyList(), groupKey) }
        }

    @Test
    fun `Given episode, When toggleLikedEpisode returns true, Then delegates to localDataSource`() =
        runTest {
            // Given
            val episode = episodeTestData
            coEvery { episodeLocalDataSource.toggleLikedEpisode(any()) } returns true

            // When
            val result = repository.toggleLikedEpisode(episode)

            // Then
            assertEquals(true, result)
            coVerify { episodeLocalDataSource.toggleLikedEpisode(any()) }
        }

    @Test
    fun `Given episode, When toggleLikedEpisode returns false, Then delegates to localDataSource`() =
        runTest {
            // Given
            val episode = episodeTestData
            coEvery { episodeLocalDataSource.toggleLikedEpisode(any()) } returns false

            // When
            val result = repository.toggleLikedEpisode(episode)

            // Then
            assertEquals(false, result)
            coVerify { episodeLocalDataSource.toggleLikedEpisode(any()) }
        }

    @Test
    fun `Given id and position, When updatePlayed, Then delegates to localDataSource`() = runTest {
        // Given
        val id = episodeTestData.id
        val position = 30.seconds
        val isCompleted = false

        // When
        repository.updatePlayed(id = id, position = position, isCompleted = isCompleted)

        // Then
        coVerify { episodeLocalDataSource.updatePlayedEpisode(any()) }
    }

    @Test
    fun `Given id and duration, When updateEpisodeDuration, Then delegates to localDataSource`() =
        runTest {
            // Given
            val id = 1L
            val duration = Duration.parse("1h")

            // When
            repository.updateEpisodeDuration(id, duration)

            // Then
            coVerify { episodeLocalDataSource.updateEpisodeDuration(id, duration) }
        }

    @Test
    fun `Given groupKey, When getEpisodesByGroupKey, Then delegates to localDataSource`() =
        runTest {
            // Given
            val groupKey = "feedId:123"
            every {
                episodeLocalDataSource.getEpisodesByGroupKey(groupKey, Int.MAX_VALUE)
            } returns kotlinx.coroutines.flow.flowOf(emptyList())

            // When
            val result = repository.getEpisodesByGroupKey(groupKey)

            // Then
            assertEquals(emptyList<Any>(), result)
            coVerify { episodeLocalDataSource.getEpisodesByGroupKey(groupKey, Int.MAX_VALUE) }
        }

    @Test
    fun `Given id, When removeSavedEpisode, Then delegates to localDataSource`() = runTest {
        // Given
        val id = 42L

        // When
        repository.removeSavedEpisode(id)

        // Then
        coVerify { episodeLocalDataSource.removeSavedEpisode(id) }
    }

    @Test
    fun `Given url, When fetchChapters, Then delegates to chapterRemoteDataSource`() = runTest {
        // Given
        val url = "https://example.com/chapters.json"
        coEvery { chapterRemoteDataSource.fetchChapters(url) } returns emptyList()

        // When
        val result = repository.fetchChapters(url)

        // Then
        assertEquals(emptyList<Any>(), result)
        coVerify { chapterRemoteDataSource.fetchChapters(url) }
    }

    @Test
    fun `Given ids, When getEpisodesByIds, Then delegates to localDataSource`() = runTest {
        val ids = listOf(1L, 2L, 3L)
        every { episodeLocalDataSource.getEpisodesByIds(ids) } returns flowOf(emptyList())

        repository.getEpisodesByIds(ids).test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
        coVerify { episodeLocalDataSource.getEpisodesByIds(ids) }
    }

    @Test
    fun `Given episode, When isLikedEpisode, Then delegates to localDataSource`() = runTest {
        val episode = episodeTestData
        every { episodeLocalDataSource.isLikedEpisode(any()) } returns flowOf(true)

        repository.isLikedEpisode(episode).test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given feedId, When getEpisodesByFeedId, Then fetches from remote`() = runTest {
        val feedId = 5778530L
        coEvery { episodeRemoteDataSource.getEpisodesByFeedId(feedId, 10) } returns emptyList()

        repository.getEpisodesByFeedId(feedId, 10).test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
        coVerify { episodeRemoteDataSource.getEpisodesByFeedId(feedId, 10) }
    }
}
