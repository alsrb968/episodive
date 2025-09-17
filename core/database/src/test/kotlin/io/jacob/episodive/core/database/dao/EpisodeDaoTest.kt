package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import app.cash.turbine.test
import io.jacob.episodive.core.database.RoomDatabaseRule
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.mapper.toEpisodeEntity
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import io.jacob.episodive.core.testing.data.episodeTestData
import io.jacob.episodive.core.testing.data.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class EpisodeDaoTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val dbRule = RoomDatabaseRule()

    private lateinit var dao: EpisodeDao

    @Before
    fun setup() {
        dao = dbRule.db.episodeDao()
    }

    private val episodeEntity = episodeTestData.toEpisodeEntity()
    private val episodeEntities = episodeTestDataList.toEpisodeEntities()

    @Test
    fun `Given a episode entity, When upsertEpisode is called, Then the episode is inserted or updated`() =
        runTest {
            // Given
            dao.upsertEpisode(episodeEntity)

            // When
            dao.getEpisode(episodeTestData.id).test {
                val episode = awaitItem()
                // Then
                assertEquals(episodeEntity, episode)
                cancel()
            }
        }

    @Test
    fun `Given list episode entities, When upsertEpisodes is called, Then the episodes are inserted or updated`() =
        runTest {
            // Given
            dao.upsertEpisodes(episodeEntities)

            // When
            dao.getEpisodes().test {
                val episodes = awaitItem()
                // Then
                assertTrue(episodes.containsAll(episodeEntities))
                cancel()
            }
        }

    @Test
    fun `Given some episode entities, When getEpisodesPaging is called, Then a PagingSource is returned`() =
        runTest {
            // Given
            dao.upsertEpisodes(episodeEntities)

            // When
            val result = dao.getEpisodesPaging().load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )

            val episodes = (result as PagingSource.LoadResult.Page).data
            // Then
            assertEquals(10, episodes.size)
            assertTrue(episodes.containsAll(episodeEntities))
        }

    @Test
    fun `Given some episode entities, When deleteEpisode is called, Then the episode is deleted`() =
        runTest {
            // Given
            dao.upsertEpisodes(episodeEntities)

            // When
            dao.deleteEpisode(episodeEntity.id)
            dao.getEpisodes().test {
                val episodes = awaitItem()
                // Then
                assertFalse(episodes.contains(episodeEntity))
                cancel()
            }
            assertEquals(9, dao.getEpisodeCount().first())
        }

    @Test
    fun `Given some episode entity liked and some episode entities, When getLikedEpisodes is called, Then liked episodes are returned`() =
        runTest {
            // Given
            val likedAt = Clock.System.now()
            dao.addLike(LikedEpisodeEntity(episodeEntities[0].id, likedAt))
            dao.addLike(LikedEpisodeEntity(episodeEntities[1].id, likedAt.plus(1.minutes)))
            dao.addLike(LikedEpisodeEntity(episodeEntities[2].id, likedAt.plus(2.minutes)))
            dao.upsertEpisodes(episodeEntities)

            // When
            val likedEpisodes = dao.getLikedEpisodes().first()

            // Then
            assertEquals(3, likedEpisodes.size)
            assertEquals(episodeEntities[2], likedEpisodes[0])
            assertEquals(episodeEntities[1], likedEpisodes[1])
            assertEquals(episodeEntities[0], likedEpisodes[2])
        }

    @Test
    fun `Given some episode entity liked, When isLiked is called, Then true is returned`() =
        runTest {
            // Given
            val likedAt = Clock.System.now()
            dao.addLike(LikedEpisodeEntity(episodeEntity.id, likedAt))
            dao.upsertEpisode(episodeEntity)

            // When
            val isLiked = dao.isLiked(episodeEntity.id).first()

            // Then
            assertTrue(isLiked)
        }

    @Test
    fun `Given some episode entities likes, When some episode entities removed, Then getLikedEpisodeCount returns correct count`() =
        runTest {
            // Given
            val likedAt = Clock.System.now()
            dao.addLike(LikedEpisodeEntity(episodeEntities[0].id, likedAt))
            dao.addLike(LikedEpisodeEntity(episodeEntities[1].id, likedAt.plus(1.minutes)))
            dao.addLike(LikedEpisodeEntity(episodeEntities[2].id, likedAt.plus(2.minutes)))
            dao.upsertEpisodes(episodeEntities)
            assertEquals(3, dao.getLikedEpisodeCount().first())

            // When
            dao.removeLike(episodeEntities[0].id)
            assertEquals(2, dao.getLikedEpisodeCount().first())

            // When
            dao.removeLike(episodeEntities[1].id)
            assertEquals(1, dao.getLikedEpisodeCount().first())

            // When
            dao.removeLike(episodeEntities[2].id)
            assertEquals(0, dao.getLikedEpisodeCount().first())
        }
}