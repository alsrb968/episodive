package io.jacob.episodive.core.network.datasource

import io.jacob.episodive.core.network.api.EpisodeApi
import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.network.model.ResponseListWrapper
import io.jacob.episodive.core.network.model.ResponseWrapper
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EpisodeRemoteDataSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodeApi = mockk<EpisodeApi>(relaxed = true)

    private val dataSource: EpisodeRemoteDataSource = EpisodeRemoteDataSourceImpl(
        episodeApi = episodeApi,
    )

    @Test
    fun `Given dependencies, when searchEpisodesByPerson called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.searchEpisodesByPerson(any(), any())
            } returns dataList

            // When
            dataSource.searchEpisodesByPerson("John Doe")

            // Then
            coVerifySequence {
                episodeApi.searchEpisodesByPerson(any(), any())
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getEpisodesByFeedId called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { liveEpisodes } returns null
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.getEpisodesByFeedId(any(), any(), any())
            } returns dataList

            // When
            dataSource.getEpisodesByFeedId(123L)

            // Then
            coVerifySequence {
                episodeApi.getEpisodesByFeedId(any(), any(), any())
                dataList.liveEpisodes
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getEpisodesByFeedUrl called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.getEpisodesByFeedUrl(any(), any(), any())
            } returns dataList

            // When
            dataSource.getEpisodesByFeedUrl("https://example.com/feed")

            // Then
            coVerifySequence {
                episodeApi.getEpisodesByFeedUrl(any(), any(), any())
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getEpisodesByPodcastGuid called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.getEpisodesByPodcastGuid(any(), any(), any())
            } returns dataList

            // When
            dataSource.getEpisodesByPodcastGuid("some-guid")

            // Then
            coVerifySequence {
                episodeApi.getEpisodesByPodcastGuid(any(), any(), any())
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getEpisodeById called, then dao called`() =
        runTest {
            // Given
            val data = mockk<ResponseWrapper<EpisodeResponse>>(relaxed = true) {
                every { data } returns mockk()
            }
            coEvery {
                episodeApi.getEpisodeById(any())
            } returns data

            // When
            val result = dataSource.getEpisodeById(42551776753L)

            // Then
            coVerifySequence {
                episodeApi.getEpisodeById(any())
                data.data
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getLiveEpisodes called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.getLiveEpisodes(any())
            } returns dataList

            // When
            dataSource.getLiveEpisodes()

            // Then
            coVerifySequence {
                episodeApi.getLiveEpisodes(any())
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getRandomEpisodes called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.getRandomEpisodes(any(), any(), any(), any())
            } returns dataList

            // When
            dataSource.getRandomEpisodes(5)

            // Then
            coVerifySequence {
                episodeApi.getRandomEpisodes(any(), any(), any(), any())
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }

    @Test
    fun `Given dependencies, when getRecentEpisodes called, then dao called`() =
        runTest {
            // Given
            val dataList = mockk<ResponseListWrapper<EpisodeResponse>>(relaxed = true) {
                every { dataList } returns emptyList()
            }
            coEvery {
                episodeApi.getRecentEpisodes(any(), any())
            } returns dataList

            // When
            dataSource.getRecentEpisodes(10)

            // Then
            coVerifySequence {
                episodeApi.getRecentEpisodes(any(), any())
                dataList.dataList
            }
            confirmVerified(episodeApi)
        }
}