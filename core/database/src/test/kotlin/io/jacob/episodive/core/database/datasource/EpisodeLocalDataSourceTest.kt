package io.jacob.episodive.core.database.datasource

import androidx.room.withTransaction
import io.jacob.episodive.core.database.EpisodiveDatabase
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.mapper.toEpisodeEntity
import io.jacob.episodive.core.testing.data.episodeTestData
import io.jacob.episodive.core.testing.data.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

class EpisodeLocalDataSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val database = mockk<EpisodiveDatabase>(relaxed = true)
    private val episodeDao = mockk<EpisodeDao>(relaxed = true)

    private val dataSource: EpisodeLocalDataSource =
        EpisodeLocalDataSourceImpl(
            database = database,
            episodeDao = episodeDao,
        )

    private val episodeEntity = episodeTestData.toEpisodeEntity()
    private val episodeEntities = episodeTestDataList.toEpisodeEntities()

    @Test
    fun `Given dependencies, When upsertEpisode is called, Then upsertEpisode of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.upsertEpisode(any()) } just Runs

            // When
            dataSource.upsertEpisode(episodeEntity)

            // Then
            coVerify { episodeDao.upsertEpisode(episodeEntity) }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When upsertEpisodes is called, Then upsertEpisodes of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.upsertEpisodes(any()) } just Runs

            // When
            dataSource.upsertEpisodes(episodeEntities)

            // Then
            coVerify { episodeDao.upsertEpisodes(episodeEntities) }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getEpisode is called, Then getEpisode of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getEpisode(any()) } returns mockk()

            // When
            dataSource.getEpisode(episodeEntity.id)

            // Then
            coVerify { episodeDao.getEpisode(episodeEntity.id) }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getEpisodes is called, Then getEpisodes of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getEpisodes() } returns mockk()

            // When
            dataSource.getEpisodes()

            // Then
            coVerify { episodeDao.getEpisodes() }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getEpisodesPaging is called, Then getEpisodesPaging of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getEpisodesPaging() } returns mockk()

            // When
            dataSource.getEpisodesPaging()

            // Then
            coVerify { episodeDao.getEpisodesPaging() }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getLikedEpisodes is called, Then getLikedEpisodes of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getLikedEpisodes() } returns mockk()

            // When
            dataSource.getLikedEpisodes()

            // Then
            coVerify { episodeDao.getLikedEpisodes() }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When isLiked is called, Then isLiked of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.isLiked(any()) } returns mockk()

            // When
            dataSource.isLiked(episodeEntity.id)

            // Then
            coVerify { episodeDao.isLiked(episodeEntity.id) }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given default parameters, When toggleLike is called, Then addLike is called with current time`() =
        runTest {
            // Given
            mockkStatic("androidx.room.RoomDatabaseKt")
            val transactionSlot = slot<suspend () -> Unit>()
            val fixedInstant = Clock.System.now()
            coEvery { episodeDao.isLiked(any()) } returns flowOf(false)
            coEvery {
                database.withTransaction(capture(transactionSlot))
            } coAnswers {
                transactionSlot.captured.invoke()
            }
            coEvery { episodeDao.addLike(any()) } just Runs
            coEvery { episodeDao.removeLike(any()) } just Runs

            // When
            dataSource.toggleLike(episodeEntity.id, fixedInstant)

            // Then
            coVerifySequence {
                episodeDao.isLiked(episodeEntity.id)
                database.withTransaction(any<suspend () -> Unit>())
                episodeDao.addLike(match { it.id == episodeEntity.id && it.likedAt == fixedInstant })
            }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given isLiked return false, When toggleLike is called, Then addLike is called`() =
        runTest {
            // Given
            mockkStatic("androidx.room.RoomDatabaseKt")
            val transactionSlot = slot<suspend () -> Unit>()
            coEvery { episodeDao.isLiked(any()) } returns flowOf(false)
            coEvery {
                database.withTransaction(capture(transactionSlot))
            } coAnswers {
                transactionSlot.captured.invoke()
            }
            coEvery { episodeDao.addLike(any()) } just Runs
            coEvery { episodeDao.removeLike(any()) } just Runs

            // When
            dataSource.toggleLike(episodeEntity.id)

            // Then
            coVerifySequence {
                episodeDao.isLiked(episodeEntity.id)
                database.withTransaction(any<suspend () -> Unit>())
                episodeDao.addLike(any())
            }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given isLiked return true, When toggleLike is called, Then removeLike is called`() =
        runTest {
            // Given
            mockkStatic("androidx.room.RoomDatabaseKt")
            val transactionSlot = slot<suspend () -> Unit>()
            coEvery { episodeDao.isLiked(any()) } returns flowOf(true)
            coEvery {
                database.withTransaction(capture(transactionSlot))
            } coAnswers {
                transactionSlot.captured.invoke()
            }
            coEvery { episodeDao.addLike(any()) } just Runs
            coEvery { episodeDao.removeLike(any()) } just Runs

            // When
            dataSource.toggleLike(episodeEntity.id)

            // Then
            coVerifySequence {
                episodeDao.isLiked(episodeEntity.id)
                database.withTransaction(any<suspend () -> Unit>())
                episodeDao.removeLike(episodeEntity.id)
            }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When deleteEpisode is called, Then deleteEpisode of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.deleteEpisode(any()) } just Runs

            // When
            dataSource.deleteEpisode(episodeEntity.id)

            // Then
            coVerify { episodeDao.deleteEpisode(episodeEntity.id) }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When deleteEpisodes is called, Then deleteEpisodes of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.deleteEpisodes() } just Runs

            // When
            dataSource.deleteEpisodes()

            // Then
            coVerify { episodeDao.deleteEpisodes() }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getEpisodeCount is called, Then getEpisodeCount of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getEpisodeCount() } returns mockk()

            // When
            dataSource.getEpisodeCount()

            // Then
            coVerify { episodeDao.getEpisodeCount() }
            confirmVerified(
                database,
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getLikedEpisodeCount is called, Then getLikedEpisodeCount of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getLikedEpisodeCount() } returns mockk()

            // When
            dataSource.getLikedEpisodeCount()

            // Then
            coVerify { episodeDao.getLikedEpisodeCount() }
            confirmVerified(
                database,
                episodeDao,
            )
        }
}