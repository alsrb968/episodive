package io.jacob.episodive.core.database.dao

import app.cash.turbine.test
import io.jacob.episodive.core.database.RoomDatabaseRule
import io.jacob.episodive.core.database.mapper.toPodcastEntity
import io.jacob.episodive.core.testing.data.podcastTestData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
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

    @Test
    fun `Given a podcast entity, When upsertPodcast is called, Then the podcast is inserted or updated`() =
        runTest {
            // Given
            val entity = podcastTestData.toPodcastEntity()

            // When
            dao.upsertPodcast(entity)

            // Then
            dao.getPodcasts().test {
                val podcasts = awaitItem()
                assertTrue(podcasts.contains(entity))
                cancel()
            }
        }
}