package io.jacob.episodive.core.database.datasource

import io.jacob.episodive.core.database.dao.PodcastDao
import io.jacob.episodive.core.database.mapper.toPodcastEntities
import io.jacob.episodive.core.database.mapper.toPodcastEntity
import io.jacob.episodive.core.testing.model.podcastTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PodcastLocalDataSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastDao = mockk<PodcastDao>(relaxed = true)

    private val dataSource: PodcastLocalDataSource =
        PodcastLocalDataSourceImpl(
            podcastDao = podcastDao,
        )

    private val podcastEntity = podcastTestData.toPodcastEntity()
    private val podcastEntities = podcastTestDataList.toPodcastEntities()

    @Test
    fun `Given dependencies, When upsertPodcast is called, Then upsertPodcast of dao is called`() =
        runTest {
            // Given
            coEvery { podcastDao.upsertPodcast(any()) } just Runs

            // When
            dataSource.upsertPodcast(podcastEntity)

            // Then
            coVerify { podcastDao.upsertPodcast(podcastEntity) }
            confirmVerified(
                podcastDao,
            )
        }

    @Test
    fun `Given dependencies, When upsertPodcasts is called, Then upsertPodcasts of dao is called`() =
        runTest {
            // Given
            coEvery { podcastDao.upsertPodcasts(any()) } just Runs

            // When
            dataSource.upsertPodcasts(podcastEntities)

            // Then
            coVerify { podcastDao.upsertPodcasts(podcastEntities) }
            confirmVerified(
                podcastDao,
            )
        }

    @Test
    fun `Given dependencies, When getPodcast is called, Then getPodcast of dao is called`() =
        runTest {
            // Given
            coEvery { podcastDao.getPodcast(any()) } returns mockk()

            // When
            dataSource.getPodcast(podcastEntity.id)

            // Then
            coVerify { podcastDao.getPodcast(podcastEntity.id) }
            confirmVerified(
                podcastDao,
            )
        }

    @Test
    fun `Given dependencies, When getPodcasts is called, Then getPodcasts of dao is called`() =
        runTest {
            // Given
            coEvery { podcastDao.getPodcasts() } returns mockk()

            // When
            dataSource.getPodcasts()

            // Then
            coVerify { podcastDao.getPodcasts() }
            confirmVerified(
                podcastDao,
            )
        }

    @Test
    fun `Given dependencies, When deletePodcast is called, Then deletePodcast of dao is called`() =
        runTest {
            // Given
            coEvery { podcastDao.deletePodcast(any()) } just Runs

            // When
            dataSource.deletePodcast(podcastEntity.id)

            // Then
            coVerify { podcastDao.deletePodcast(podcastEntity.id) }
            confirmVerified(
                podcastDao,
            )
        }

    @Test
    fun `Given dependencies, When deletePodcasts is called, Then deletePodcasts of dao is called`() =
        runTest {
            // Given
            coEvery { podcastDao.deletePodcasts() } just Runs

            // When
            dataSource.deletePodcasts()

            // Then
            coVerify { podcastDao.deletePodcasts() }
            confirmVerified(
                podcastDao,
            )
        }
}