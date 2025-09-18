package io.jacob.episodive.core.database.dao

import app.cash.turbine.test
import io.jacob.episodive.core.database.RoomDatabaseRule
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.mapper.toEpisodeEntity
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import io.jacob.episodive.core.database.model.PlayedEpisodeEntity
import io.jacob.episodive.core.testing.data.episodeTestData
import io.jacob.episodive.core.testing.data.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    private val cacheKey = "test_cache"
    private val episodeEntity = episodeTestData.toEpisodeEntity(cacheKey = cacheKey)
    private val episodeEntities = episodeTestDataList.toEpisodeEntities(cacheKey = cacheKey)

    @Test
    fun `Given a episode entity, When upsertEpisode is called, Then the episode is inserted or updated`() =
        runTest {
            // Given
            dao.upsertEpisode(episodeEntity)

            // When
            dao.getEpisode(episodeTestData.id).test {
                val episode = awaitItem()
                // Then
                assertEquals(episodeEntity.id, episode?.id)
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
                val episodeIds = episodeEntities.map { it.id }
                val entityIds = episodes.map { it.id }
                assertTrue(episodeIds.containsAll(entityIds))
                cancel()
            }
        }

    @Test
    fun `Given some episode entities, When getEpisodesByCacheKey is called, Then the episodes with the cache key are returned`() =
        runTest {
            // Given
            val entities = episodeEntities.chunked(3)
            dao.upsertEpisodes(entities[0].map { it.copy(cacheKey = "test_key1") })
            dao.upsertEpisodes(entities[1].map { it.copy(cacheKey = "test_key2") })
            dao.upsertEpisodes(entities[2].map { it.copy(cacheKey = "test_key3") })
            dao.upsertEpisodes(entities[3])

            // When
            dao.getEpisodesByCacheKey("test_key1").test {
                val episodes = awaitItem()
                // Then
                assertEquals(entities[0].size, episodes.size)
                val episodeIds = entities[0].map { it.id }
                val entityIds = episodes.map { it.id }
                assertTrue(episodeIds.containsAll(entityIds))
                cancel()
            }
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
    fun `Given some episode entities, When deleteEpisodes is called, Then all episodes are deleted`() =
        runTest {
            // Given
            dao.upsertEpisodes(episodeEntities)

            // When
            dao.deleteEpisodes()
            dao.getEpisodes().test {
                val episodes = awaitItem()
                // Then
                assertTrue(episodes.isEmpty())
                cancel()
            }
            assertEquals(0, dao.getEpisodeCount().first())
        }

    @Test
    fun `Given some episode entity liked and some episode entities, When getLikedEpisodes is called, Then liked episodes are returned`() =
        runTest {
            // Given
            val likedAt = Clock.System.now()
            dao.addLiked(LikedEpisodeEntity(episodeEntities[0].id, likedAt))
            dao.addLiked(LikedEpisodeEntity(episodeEntities[1].id, likedAt.plus(1.minutes)))
            dao.addLiked(LikedEpisodeEntity(episodeEntities[2].id, likedAt.plus(2.minutes)))
            dao.upsertEpisodes(episodeEntities)

            // When
            val likedEpisodes = dao.getLikedEpisodes().first()

            // Then
            assertEquals(3, likedEpisodes.size)
            assertEquals(episodeEntities[2].id, likedEpisodes[0].episode?.id)
            assertEquals(episodeEntities[1].id, likedEpisodes[1].episode?.id)
            assertEquals(episodeEntities[0].id, likedEpisodes[2].episode?.id)
        }

    @Test
    fun `Given some episode entity liked, When isLiked is called, Then true is returned`() =
        runTest {
            // Given
            val likedAt = Clock.System.now()
            dao.addLiked(LikedEpisodeEntity(episodeEntity.id, likedAt))
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
            dao.addLiked(LikedEpisodeEntity(episodeEntities[0].id, likedAt))
            dao.addLiked(LikedEpisodeEntity(episodeEntities[1].id, likedAt.plus(1.minutes)))
            dao.addLiked(LikedEpisodeEntity(episodeEntities[2].id, likedAt.plus(2.minutes)))
            dao.upsertEpisodes(episodeEntities)
            assertEquals(3, dao.getLikedEpisodeCount().first())

            // When
            dao.removeLiked(episodeEntities[0].id)
            assertEquals(2, dao.getLikedEpisodeCount().first())

            // When
            dao.removeLiked(episodeEntities[1].id)
            assertEquals(1, dao.getLikedEpisodeCount().first())

            // When
            dao.removeLiked(episodeEntities[2].id)
            assertEquals(0, dao.getLikedEpisodeCount().first())
        }

    @Test
    fun `Given some episode entities, When getPlayingEpisodes is called, Then playing episodes are returned`() =
        runTest {
            // Given
            val now = Clock.System.now()
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[0].id,
                    playedAt = now,
                    position = 1000.seconds,
                    isCompleted = false
                )
            )
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[1].id,
                    playedAt = now.plus(1.minutes),
                    position = 2000.seconds,
                    isCompleted = false
                )
            )
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[2].id,
                    playedAt = now.plus(2.minutes),
                    position = 3000.seconds,
                    isCompleted = true
                )
            )
            dao.upsertEpisodes(episodeEntities)

            // When
            val playingEpisodes = dao.getPlayingEpisodes().first()

            // Then
            assertEquals(2, playingEpisodes.size)
            assertEquals(episodeEntities[1].id, playingEpisodes[0].episode?.id)
            assertEquals(2000.seconds, playingEpisodes[0].position)
            assertEquals(episodeEntities[0].id, playingEpisodes[1].episode?.id)
            assertEquals(1000.seconds, playingEpisodes[1].position)
        }

    @Test
    fun `Given some episode entities, When getPlayedEpisodes is called, Then played episodes are returned`() =
        runTest {
            // Given
            val now = Clock.System.now()
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[0].id,
                    playedAt = now,
                    position = 1000.seconds,
                    isCompleted = false
                )
            )
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[1].id,
                    playedAt = now.plus(1.minutes),
                    position = 2000.seconds,
                    isCompleted = false
                )
            )
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[2].id,
                    playedAt = now.plus(2.minutes),
                    position = 3000.seconds,
                    isCompleted = true
                )
            )
            dao.upsertEpisodes(episodeEntities)

            // When
            val playedEpisodes = dao.getPlayedEpisodes().first()

            // Then
            assertEquals(1, playedEpisodes.size)
            assertEquals(episodeEntities[2].id, playedEpisodes[0].episode?.id)
            assertEquals(3000.seconds, playedEpisodes[0].position)
        }

    @Test
    fun `Given some episode entities, When upsert and remove, Then getPlayingEpisodeCount returns correct count`() =
        runTest {
            // Given
            val now = Clock.System.now()
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[0].id,
                    playedAt = now,
                    position = 1000.seconds,
                    isCompleted = false
                )
            )
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[1].id,
                    playedAt = now.plus(1.minutes),
                    position = 2000.seconds,
                    isCompleted = false
                )
            )
            dao.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntities[2].id,
                    playedAt = now.plus(2.minutes),
                    position = 3000.seconds,
                    isCompleted = true
                )
            )
            dao.upsertEpisodes(episodeEntities)
            assertEquals(2, dao.getPlayingEpisodeCount().first())

            // When
            dao.removePlayed(episodeEntities[0].id)
            assertEquals(1, dao.getPlayingEpisodeCount().first())

            // When
            dao.removePlayed(episodeEntities[1].id)
            assertEquals(0, dao.getPlayingEpisodeCount().first())
        }
}