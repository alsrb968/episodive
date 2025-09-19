package io.jacob.episodive.core.database.dao

import app.cash.turbine.test
import io.jacob.episodive.core.database.RoomDatabaseRule
import io.jacob.episodive.core.database.mapper.toPodcastEntities
import io.jacob.episodive.core.database.mapper.toPodcastEntity
import io.jacob.episodive.core.testing.data.podcastTestData
import io.jacob.episodive.core.testing.data.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PodcastDaoTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val dbRule = RoomDatabaseRule()

    private lateinit var dao: PodcastDao

    @Before
    fun setup() {
        dao = dbRule.db.podcastDao()
    }

    private val podcastEntity = podcastTestData.toPodcastEntity()
    private val podcastEntities = podcastTestDataList.toPodcastEntities()

    @Test
    fun `Given a podcast entity, When upsertPodcast is called, Then the podcast is inserted or updated`() =
        runTest {
            // Given
            dao.upsertPodcast(podcastEntity)

            // When
            dao.getPodcast(podcastTestData.id).test {
                val podcast = awaitItem()
                // Then
                assertEquals(podcastEntity, podcast)
                cancel()
            }
        }

    @Test
    fun `Given list podcast entities, When upsertPodcasts is called, Then the podcasts are inserted or updated`() =
        runTest {
            // Given
            dao.upsertPodcasts(podcastEntities)

            // When
            dao.getPodcasts().test {
                val podcasts = awaitItem()
                // Then
                assertEquals(10, podcasts.size)
                assertTrue(podcasts.containsAll(podcastEntities))
                cancel()
            }
        }

    @Test
    fun `Given a podcast entity, When deletePodcast is called, Then the podcast is deleted`() =
        runTest {
            // Given
            dao.upsertPodcasts(podcastEntities)

            // When
            dao.deletePodcast(podcastTestData.id)

            dao.getPodcasts().test {
                val podcasts = awaitItem()
                // Then
                assertEquals(9, podcasts.size)
                assertFalse(podcasts.contains(podcastEntity))
                cancel()
            }
        }

    @Test
    fun `Given some podcast entities, When deletePodcasts is called, Then all podcasts are deleted`() =
        runTest {
            // Given
            dao.upsertPodcasts(podcastEntities)

            // When
            dao.deletePodcasts()

            dao.getPodcasts().test {
                val podcasts = awaitItem()
                // Then
                assertTrue(podcasts.isEmpty())
                cancel()
            }
        }
}