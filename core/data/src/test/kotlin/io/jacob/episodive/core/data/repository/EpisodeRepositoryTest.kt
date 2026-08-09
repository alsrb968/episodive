package io.jacob.episodive.core.data.repository

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.jacob.episodive.core.data.util.query.EpisodeQuery
import io.jacob.episodive.core.data.util.query.QueryScope
import io.jacob.episodive.core.data.util.updater.EpisodeRemoteUpdater
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.SoundbiteLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntity
import io.jacob.episodive.core.database.mapper.toEpisodeWithExtrasViews
import io.jacob.episodive.core.database.model.EpisodeWithExtrasView
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.network.datasource.ChapterRemoteDataSource
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.datasource.SoundbiteRemoteDataSource
import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class EpisodeRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val localDataSource = mockk<EpisodeLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<EpisodeRemoteDataSource>(relaxed = true)
    private val chapterRemoteDataSource = mockk<ChapterRemoteDataSource>(relaxed = true)
    private val soundbiteLocalDataSource = mockk<SoundbiteLocalDataSource>(relaxed = true)
    private val soundbiteRemoteDataSource = mockk<SoundbiteRemoteDataSource>(relaxed = true)
    private val remoteUpdater = mockk<EpisodeRemoteUpdater.Factory>(relaxed = true)

    private val repository: EpisodeRepository = EpisodeRepositoryImpl(
        episodeLocalDataSource = localDataSource,
        episodeRemoteDataSource = remoteDataSource,
        chapterRemoteDataSource = chapterRemoteDataSource,
        soundbiteLocalDataSource = soundbiteLocalDataSource,
        soundbiteRemoteDataSource = soundbiteRemoteDataSource,
        remoteUpdater = remoteUpdater,
    )

    private val episodeDtos = episodeTestDataList.toEpisodeWithExtrasViews()

    @After
    fun teardown() {
        confirmVerified(
            localDataSource,
            remoteDataSource,
            remoteUpdater,
            chapterRemoteDataSource,
            soundbiteLocalDataSource,
            soundbiteRemoteDataSource
        )
    }

    @Test
    fun `Given person, When searchEpisodesByPerson, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val person = "John Doe"
            val expectedQuery = EpisodeQuery.Person(person)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery { mockUpdater.getFlowList(any()) } returns flowOf(episodeDtos)
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            repository.searchEpisodesByPerson(person, max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(max, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getFlowList(any())
            }
        }

    @Test
    fun `Given feedId, When getEpisodesByFeedId, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val feedId = 123L

            coEvery {
                remoteDataSource.getEpisodesByFeedId(any(), any())
            } returns listOf(mockk<EpisodeResponse>(relaxed = true))

            // When
            repository.getEpisodesByFeedId(feedId, max = max).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodesByFeedId(feedId, max)
            }
        }

    @Test
    fun `Given feedId, When getEpisodesByFeedIdPaging, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val feedId = 123L
            val expectedQuery = EpisodeQuery.FeedId(feedId)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                mockUpdater.getPagingData(any())
            } returns flowOf(PagingData.from(episodeDtos))
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            val result = repository.getEpisodesByFeedIdPaging(feedId).asSnapshot()

            // Then
            assertEquals(episodeTestDataList.size, result.size)
            assertEquals(episodeTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getPagingData(any())
            }
        }

    @Test
    fun `Given feedUrl, When getEpisodesByFeedUrl, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val feedUrl = "https://example.com/feed.xml"
            val expectedQuery = EpisodeQuery.FeedUrl(feedUrl)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery { mockUpdater.getFlowList(any()) } returns flowOf(episodeDtos)
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            repository.getEpisodesByFeedUrl(feedUrl, max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getFlowList(any())
            }
        }

    @Test
    fun `Given podcastGuid, When getEpisodesByPodcastGuid, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val guid = "test-podcast-guid"
            val expectedQuery = EpisodeQuery.PodcastGuid(guid)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery { mockUpdater.getFlowList(any()) } returns flowOf(episodeDtos)
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            repository.getEpisodesByPodcastGuid(guid, max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getFlowList(any())
            }
        }

    @Test
    fun `When getLiveEpisodes, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val expectedQuery = EpisodeQuery.Live(max)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery { mockUpdater.getFlowList(any()) } returns flowOf(episodeDtos)
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            repository.getLiveEpisodes(max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerify {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getFlowList(any())
            }
        }

    @Test
    fun `Given parameters, When getRandomEpisodes, Then calls remoteDataSource directly`() =
        runTest {
            // Given
            val max = 10
            val query = EpisodeQuery.Random(
                max = max,
                language = null,
                categories = emptyList(),
            )

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery { mockUpdater.getFlowList(any()) } returns flowOf(episodeDtos)
            coEvery { remoteUpdater.create(query) } returns mockUpdater

            // When
            repository.getRandomEpisodes(max).test {
                val result = awaitItem()

                // Then
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerify {
                remoteUpdater.create(query)
                mockUpdater.getFlowList(any())
            }
        }

    @Test
    fun `When getRecentEpisodes, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val max = 10
            val expectedQuery = EpisodeQuery.Recent(max = max)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery { mockUpdater.getFlowList(any()) } returns flowOf(episodeDtos)
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            repository.getRecentEpisodes(max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(episodeTestDataList.size, result.size)
                assertEquals(episodeTestDataList, result)
                awaitComplete()
            }
            coVerify {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getFlowList(any())
            }
        }

    @Test
    fun `When getEpisodesByIds, Then calls localDataSource directly`() =
        runTest {
            // Given
            val ids = episodeTestDataList.map { it.id }
            coEvery {
                localDataSource.getEpisodesByIds(any())
            } returns flowOf(episodeDtos)

            // When
            repository.getEpisodesByIds(ids).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                localDataSource.getEpisodesByIds(ids)
            }
        }

    @Test
    fun `When getLikedEpisodes, Then calls localDataSource directly`() =
        runTest {
            // Given
            val max = 10
            val dtos = episodeDtos.mapIndexed { index, dto ->
                dto.copy(likedAt = Instant.fromEpochSeconds(1757883600L + index))
            }
            coEvery { localDataSource.getLikedEpisodes(limit = max) } returns flowOf(dtos)

            // When
            repository.getLikedEpisodes(max = max).test {
                val result = awaitItem()
                // Then
                assertEquals(10, result.size)
                assertEquals(dtos[0].episode.id, result[0].id)
                assertEquals(dtos[1].episode.id, result[1].id)
                awaitComplete()
            }

            // Then
            coVerifySequence {
                localDataSource.getLikedEpisodes(limit = max)
            }
        }

    @Test
    fun `When getPlayedEpisodes, Then calls localDataSource directly`() =
        runTest {
            // Given
            coEvery {
                localDataSource.getPlayedEpisodes(
                    any(),
                    any(),
                    any()
                )
            } returns flowOf(mockk<List<EpisodeWithExtrasView>>(relaxed = true))


            // When
            repository.getPlayedEpisodes(max = 10).test {
                val result = awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                localDataSource.getPlayedEpisodes(limit = 10)
            }
        }

    @Test
    fun `Given dependencies, When toggleLiked, Then calls localDataSource toggleLiked`() =
        runTest {
            // Given
            val episode = episodeTestData.toEpisodeEntity()
            coEvery { localDataSource.toggleLikedEpisode(episode) } returns true

            // When
            repository.toggleLikedEpisode(episodeTestData)

            // Then
            coVerifySequence {
                localDataSource.toggleLikedEpisode(episode)
            }
        }

    @Test
    fun `When updatePlayed, Then calls localDataSource upsertPlayed`() =
        runTest {
            // Given
            val episodeId = 123L
            val position = 30.seconds
            val isCompleted = false
            coEvery { localDataSource.updatePlayedEpisode(any()) } returns Unit

            // When
            repository.updatePlayed(episodeId, position, isCompleted)

            // Then
            coVerify {
                localDataSource.updatePlayedEpisode(
                    match {
                        it.id == episodeId && it.position == position && it.isCompleted == isCompleted
                    }
                )
            }
        }

    @Test
    fun `Given dependencies, When updateDurationOfEpisodes is called, Then calls localDataSource updateDurationOfEpisodes`() =
        runTest {
            // Given
            coEvery { localDataSource.updateEpisodeDuration(any(), any()) } just Runs

            // When
            repository.updateEpisodeDuration(123L, 30.seconds)

            // Then
            coVerifySequence {
                localDataSource.updateEpisodeDuration(123L, 30.seconds)
            }
        }

    @Test
    fun `Given dependencies, When fetchChapters is called, Then calls chapterRemoteDataSource fetchChapters`() =
        runTest {
            // Given
            val url = "https://example.com/chapters.json"
            coEvery { chapterRemoteDataSource.fetchChapters(any()) } returns emptyList()

            // When
            repository.fetchChapters(url)

            // Then
            coVerifySequence {
                chapterRemoteDataSource.fetchChapters(url)
            }
        }

    @Test
    fun `When upsertEpisode, Then calls localDataSource upsertEpisode`() =
        runTest {
            // Given
            val episode = episodeTestData
            coEvery { localDataSource.upsertEpisode(any()) } returns Unit

            // When
            repository.upsertEpisode(episode)

            // Then
            coVerifySequence {
                localDataSource.upsertEpisode(episode.toEpisodeEntity())
            }
        }

    @Test
    fun `When getEpisodeById, Then calls localDataSource getEpisodeById`() =
        runTest {
            // Given
            val episodeId = episodeTestData.id
            coEvery { localDataSource.getEpisodeById(episodeId) } returns flowOf(episodeDtos.first())

            // When
            repository.getEpisodeById(episodeId).test {
                val result = awaitItem()
                // Then
                assertEquals(episodeTestDataList[0].id, result?.id)
                awaitComplete()
            }

            // Then
            coVerifySequence {
                localDataSource.getEpisodeById(episodeId)
            }
        }

    @Test
    fun `When isLikedEpisode, Then calls localDataSource isLikedEpisode`() =
        runTest {
            // Given
            val episode = episodeTestData
            coEvery { localDataSource.isLikedEpisode(any()) } returns flowOf(true)

            // When
            repository.isLikedEpisode(episode).test {
                val result = awaitItem()
                // Then
                assertEquals(true, result)
                awaitComplete()
            }

            coVerifySequence {
                localDataSource.isLikedEpisode(episode.toEpisodeEntity())
            }
        }

    @Test
    fun `When getEpisodesByGroupKey, Then calls localDataSource with groupKey and MAX_VALUE`() =
        runTest {
            // Given
            val groupKey = "playlist"
            coEvery {
                localDataSource.getEpisodesByGroupKey(any(), any())
            } returns flowOf(episodeDtos)

            // When
            val result = repository.getEpisodesByGroupKey(groupKey)

            // Then
            assertEquals(episodeTestDataList.size, result.size)
            assertEquals(episodeTestDataList, result)
            coVerifySequence {
                localDataSource.getEpisodesByGroupKey(groupKey, Int.MAX_VALUE)
            }
        }

    @Test
    fun `Given person, When searchEpisodesByPersonPaging, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val person = "Jane Doe"
            val expectedQuery = EpisodeQuery.Person(person)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                mockUpdater.getPagingData(any())
            } returns flowOf(PagingData.from(episodeDtos))
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            val result = repository.searchEpisodesByPersonPaging(person).asSnapshot()

            // Then
            assertEquals(episodeTestDataList.size, result.size)
            assertEquals(episodeTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getPagingData(any())
            }
        }

    @Test
    fun `Given feedUrl, When getEpisodesByFeedUrlPaging, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val feedUrl = "https://example.com/feed.xml"
            val expectedQuery = EpisodeQuery.FeedUrl(feedUrl)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                mockUpdater.getPagingData(any())
            } returns flowOf(PagingData.from(episodeDtos))
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            val result = repository.getEpisodesByFeedUrlPaging(feedUrl).asSnapshot()

            // Then
            assertEquals(episodeTestDataList.size, result.size)
            assertEquals(episodeTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getPagingData(any())
            }
        }

    @Test
    fun `Given guid, When getEpisodesByPodcastGuidPaging, Then creates correct query and calls sourceFactory`() =
        runTest {
            // Given
            val guid = "test-podcast-guid"
            val expectedQuery = EpisodeQuery.PodcastGuid(guid)

            val mockUpdater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                mockUpdater.getPagingData(any())
            } returns flowOf(PagingData.from(episodeDtos))
            coEvery { remoteUpdater.create(expectedQuery) } returns mockUpdater

            // When
            val result = repository.getEpisodesByPodcastGuidPaging(guid).asSnapshot()

            // Then
            assertEquals(episodeTestDataList.size, result.size)
            assertEquals(episodeTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(expectedQuery)
                mockUpdater.getPagingData(any())
            }
        }

    @Test
    fun `When getSavedEpisodes, Then calls localDataSource getSavedEpisodes`() =
        runTest {
            // Given
            coEvery { localDataSource.getSavedEpisodes(any(), any()) } returns flowOf(episodeDtos)

            // When
            repository.getSavedEpisodes(max = 10).test {
                awaitItem()
                awaitComplete()
            }

            // Then
            coVerifySequence {
                localDataSource.getSavedEpisodes(query = null, limit = 10)
            }
        }

    @Test
    fun `When toggleSavedEpisode, Then calls localDataSource with correct filePath`() =
        runTest {
            // Given
            val episode = episodeTestData
            coEvery { localDataSource.toggleSavedEpisode(any(), any()) } returns true

            // When
            val result = repository.toggleSavedEpisode(episode)

            // Then
            assertEquals(true, result)
            coVerify {
                localDataSource.toggleSavedEpisode(any(), any())
            }
        }

    @Test
    fun `When removeSavedEpisode, Then calls localDataSource`() =
        runTest {
            // Given
            coEvery { localDataSource.removeSavedEpisode(any()) } just Runs

            // When
            repository.removeSavedEpisode(123L)

            // Then
            coVerifySequence {
                localDataSource.removeSavedEpisode(123L)
            }
        }

    @Test
    fun `When replaceEpisodes, Then calls localDataSource`() =
        runTest {
            // Given
            coEvery { localDataSource.replaceEpisodes(any(), any()) } just Runs

            // When
            repository.replaceEpisodes(episodeTestDataList, "groupKey")

            // Then
            coVerifySequence {
                localDataSource.replaceEpisodes(any(), "groupKey")
            }
        }

    @Test
    fun `When getLikedEpisodesPaging, Then returns flow of paging data`() =
        runTest {
            // Given
            coEvery { localDataSource.getLikedEpisodesPaging(any()) } returns mockk(relaxed = true)

            // When
            val flow = repository.getLikedEpisodesPaging()

            // Then
            assertNotNull(flow)
        }

    @Test
    fun `When getPlayedEpisodesPaging, Then returns flow of paging data`() =
        runTest {
            // Given
            coEvery {
                localDataSource.getPlayedEpisodesPaging(any(), any())
            } returns mockk(relaxed = true)

            // When
            val flow = repository.getPlayedEpisodesPaging()

            // Then
            assertNotNull(flow)
        }

    @Test
    fun `When getSavedEpisodesPaging, Then returns flow of paging data`() =
        runTest {
            // Given
            coEvery { localDataSource.getSavedEpisodesPaging(any()) } returns mockk(relaxed = true)

            // When
            val flow = repository.getSavedEpisodesPaging()

            // Then
            assertNotNull(flow)
        }

    @Test
    fun `When getSoundbiteEpisodesPaging, Then returns flow of paging data`() =
        runTest {
            // When
            val flow = repository.getSoundbiteEpisodesPaging(max = 10)

            // Then
            assertNotNull(flow)
        }

    @Test
    fun `When getLiveEpisodesPaging is called, Then uses full scope query`() =
        runTest {
            // 미리보기(PREVIEW)와 같은 그룹을 쓰면 먼저 캐시를 채운 쪽의 개수에 갇힌다.
            // Given
            val query = EpisodeQuery.Live(max = 100, scope = QueryScope.FULL)

            val updater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                updater.getPagingData(any())
            } returns flowOf(PagingData.from(episodeDtos))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            val result = repository.getLiveEpisodesPaging(max = 100).asSnapshot()

            // Then
            assertEquals(episodeTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(query)
                updater.getPagingData(any())
            }
        }

    @Test
    fun `Given random condition, When getRandomEpisodesPaging is called, Then uses full scope query`() =
        runTest {
            // Given
            val query = EpisodeQuery.Random(
                max = 100,
                language = "ko",
                categories = emptyList(),
                scope = QueryScope.FULL,
            )

            val updater = mockk<EpisodeRemoteUpdater>(relaxed = true)
            coEvery {
                updater.getPagingData(any())
            } returns flowOf(PagingData.from(episodeDtos))
            coEvery { remoteUpdater.create(query) } returns updater

            // When
            val result = repository.getRandomEpisodesPaging(max = 100, language = "ko").asSnapshot()

            // Then
            assertEquals(episodeTestDataList, result)

            coVerifySequence {
                remoteUpdater.create(query)
                updater.getPagingData(any())
            }
        }

    @Test
    fun `Given episodeId, When refreshEpisodeDescription, Then calls remote with fulltext true`() =
        runTest {
            // Given
            val id = episodeTestData.id
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns null

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            }
        }

    @Test
    fun `Given remote description longer than local, When refreshEpisodeDescription, Then updates local description`() =
        runTest {
            // Given
            val id = episodeTestData.id
            val localDescription = "짧은 설명"
            val longerRemoteDescription = "짧은 설명보다 훨씬 더 긴 원격 전체 설명입니다."
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns longerRemoteDescription
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse
            coEvery { localDataSource.getEpisodeDescription(id) } returns localDescription

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
                localDataSource.getEpisodeDescription(id)
                localDataSource.updateEpisodeDescription(id, longerRemoteDescription)
            }
        }

    @Test
    fun `Given remote description shorter than local, When refreshEpisodeDescription, Then does not update local description`() =
        runTest {
            // Given: 원격이 기존보다 짧게 주는 경우 퇴화를 막는다
            val id = episodeTestData.id
            val longerLocalDescription = "이미 저장돼 있는 훨씬 더 긴 로컬 설명입니다."
            val shorterRemoteDescription = "짧은 원격"
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns shorterRemoteDescription
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse
            coEvery { localDataSource.getEpisodeDescription(id) } returns longerLocalDescription

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
                localDataSource.getEpisodeDescription(id)
            }
            coVerify(exactly = 0) { localDataSource.updateEpisodeDescription(any(), any()) }
        }

    @Test
    fun `Given remote description same length as local, When refreshEpisodeDescription, Then does not update local description`() =
        runTest {
            // Given
            val id = episodeTestData.id
            val description = "길이가 똑같은 설명입니다!!"
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns description
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse
            coEvery { localDataSource.getEpisodeDescription(id) } returns description

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
                localDataSource.getEpisodeDescription(id)
            }
            coVerify(exactly = 0) { localDataSource.updateEpisodeDescription(any(), any()) }
        }

    @Test
    fun `Given local description is null, When refreshEpisodeDescription, Then writes remote description`() =
        runTest {
            // Given
            val id = episodeTestData.id
            val remoteDescription = "새로 채워질 원격 설명입니다."
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns remoteDescription
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse
            coEvery { localDataSource.getEpisodeDescription(id) } returns null

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
                localDataSource.getEpisodeDescription(id)
                localDataSource.updateEpisodeDescription(id, remoteDescription)
            }
        }

    @Test
    fun `Given remote description is null, When refreshEpisodeDescription, Then does not update local description`() =
        runTest {
            // Given
            val id = episodeTestData.id
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns null
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            }
            coVerify(exactly = 0) { localDataSource.getEpisodeDescription(any()) }
            coVerify(exactly = 0) { localDataSource.updateEpisodeDescription(any(), any()) }
        }

    @Test
    fun `Given remote description is empty, When refreshEpisodeDescription, Then does not update local description`() =
        runTest {
            // Given
            val id = episodeTestData.id
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns ""
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse

            // When
            repository.refreshEpisodeDescription(id)

            // Then
            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            }
            coVerify(exactly = 0) { localDataSource.getEpisodeDescription(any()) }
            coVerify(exactly = 0) { localDataSource.updateEpisodeDescription(any(), any()) }
        }

    @Test
    fun `Given same id called twice sequentially, When refreshEpisodeDescription, Then calls remote each time`() =
        runTest {
            // Given: refreshingEpisodeIds 는 "진행 중" 표시일 뿐 "완료" 의 영구 기록이 아니다.
            // 첫 호출이 끝나며 finally 에서 표시가 지워지므로, 순차 호출은 매번 원격을 다시 친다.
            val id = episodeTestData.id
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns "매번 다시 불려야 하는 원격 설명입니다."
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse
            coEvery { localDataSource.getEpisodeDescription(id) } returns "짧음"

            // When
            repository.refreshEpisodeDescription(id)
            repository.refreshEpisodeDescription(id)

            // Then
            coVerify(exactly = 2) { remoteDataSource.getEpisodeById(id, fulltext = true) }
            coVerify(exactly = 2) { localDataSource.getEpisodeDescription(id) }
            coVerify(exactly = 2) { localDataSource.updateEpisodeDescription(id, any()) }
        }

    @Test
    fun `Given episode already refreshed once, When list cache reverts description and refreshEpisodeDescription is called again, Then it fetches remote again`() =
        runTest {
            // Given: EpisodeDao.replaceEpisodes(→ upsertEpisodesWithGroup → upsertEpisodes) 는
            // @Upsert 라 행 전체를 교체한다. EpisodeRemoteUpdater 가 목록 캐시를 갱신하면(TTL
            // 10분~1일) 보강해 둔 description 이 원격의 잘린 값으로 되돌아갈 수 있다. 이전에는
            // refreshingEpisodeIds 가 "완료" 를 영구 기록해서 그 뒤로는 앱을 재시작하기 전까지
            // 잘린 설명이 고정됐다 — 실제로 겪은 결함이다. 지금은 요청이 끝나면(성공 포함) 표시가
            // 지워지므로 같은 에피소드를 다시 열면 재보강이 가능해야 한다.
            val id = episodeTestData.id
            val longRemoteDescription = "성공적으로 두 번 다 채워져야 하는 훨씬 더 긴 원격 설명입니다."
            // 목록 캐시 갱신이 매번 같은 짧은 값으로 되돌린다고 가정한다.
            val truncatedLocalDescription = "짧게 잘린 로컬 설명"
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns longRemoteDescription
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } returns remoteResponse
            coEvery { localDataSource.getEpisodeDescription(id) } returns truncatedLocalDescription

            // When: 첫 보강 성공 후, 목록 캐시 갱신으로 로컬 description 이 다시 짧아졌다고 가정하고
            // 같은 에피소드를 다시 연다.
            repository.refreshEpisodeDescription(id)
            repository.refreshEpisodeDescription(id)

            // Then: 두 번 다 원격을 다시 쳐서 다시 채운다 — 영구히 잘린 채 고정되지 않는다.
            coVerify(exactly = 2) { remoteDataSource.getEpisodeById(id, fulltext = true) }
            coVerify(exactly = 2) { localDataSource.getEpisodeDescription(id) }
            coVerify(exactly = 2) { localDataSource.updateEpisodeDescription(id, longRemoteDescription) }
        }

    @Test
    fun `Given remote throws on first call, When refreshEpisodeDescription is called again, Then retries remote`() =
        runTest {
            // Given: 실패를 성공으로 기록하면 앱 실행 내내 재시도가 막히는 회귀를 잡는다
            val id = episodeTestData.id
            var attempt = 0
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns "재시도 후 성공한 원격 설명입니다."
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } coAnswers {
                attempt++
                if (attempt == 1) throw RuntimeException("network error") else remoteResponse
            }
            coEvery { localDataSource.getEpisodeDescription(id) } returns "짧음"

            // When
            repository.refreshEpisodeDescription(id)
            repository.refreshEpisodeDescription(id)

            // Then
            coVerify(exactly = 2) { remoteDataSource.getEpisodeById(id, fulltext = true) }
            coVerify(exactly = 1) { localDataSource.getEpisodeDescription(id) }
            coVerify(exactly = 1) { localDataSource.updateEpisodeDescription(id, any()) }
        }

    @Test
    fun `Given remote throws, When refreshEpisodeDescription, Then returns without throwing`() =
        runTest {
            // Given: 보강은 부가 기능이라 실패해도 화면에는 영향이 없어야 한다
            val id = episodeTestData.id
            coEvery {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            } throws RuntimeException("network error")

            // When / Then: 예외 없이 정상 반환해야 한다
            repository.refreshEpisodeDescription(id)

            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            }
        }

    @Test
    fun `Given remote throws CancellationException, When refreshEpisodeDescription, Then rethrows it`() =
        runTest {
            // Given
            val id = episodeTestData.id
            coEvery {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            } throws CancellationException("cancelled")

            // When / Then
            try {
                repository.refreshEpisodeDescription(id)
                fail("CancellationException 이 다시 던져져야 한다")
            } catch (e: CancellationException) {
                // 기대한 경로: 취소는 전파돼야 한다
            }

            coVerifySequence {
                remoteDataSource.getEpisodeById(id, fulltext = true)
            }
        }

    @Test
    fun `Given concurrent calls with same id, When refreshEpisodeDescription, Then remote is called only once`() =
        runTest {
            // Given: refreshingEpisodeIds.add() 가 원자적이라 먼저 진입한 호출만 원격을 치고,
            // 뒤따르는 호출은 잠금 대기 없이 즉시 반환한다.
            val id = episodeTestData.id
            val remoteReady = CompletableDeferred<EpisodeResponse?>()
            val remoteResponse = mockk<EpisodeResponse>(relaxed = true)
            every { remoteResponse.description } returns "동시 호출에서도 한 번만 불려야 하는 설명입니다."
            coEvery { remoteDataSource.getEpisodeById(id, fulltext = true) } coAnswers { remoteReady.await() }
            coEvery { localDataSource.getEpisodeDescription(id) } returns "짧음"

            // When
            val job1 = launch { repository.refreshEpisodeDescription(id) }
            val job2 = launch { repository.refreshEpisodeDescription(id) }
            advanceUntilIdle()
            remoteReady.complete(remoteResponse)
            advanceUntilIdle()
            job1.join()
            job2.join()

            // Then
            coVerify(exactly = 1) { remoteDataSource.getEpisodeById(id, fulltext = true) }
            coVerify(exactly = 1) { localDataSource.getEpisodeDescription(id) }
            coVerify(exactly = 1) { localDataSource.updateEpisodeDescription(id, any()) }
        }
}
