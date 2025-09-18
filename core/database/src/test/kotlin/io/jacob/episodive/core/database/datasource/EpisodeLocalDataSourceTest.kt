package io.jacob.episodive.core.database.datasource

import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.mapper.toEpisodeEntity
import io.jacob.episodive.core.database.model.PlayedEpisodeEntity
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class EpisodeLocalDataSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodeDao = mockk<EpisodeDao>(relaxed = true)

    private val dataSource: EpisodeLocalDataSource =
        EpisodeLocalDataSourceImpl(
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
                episodeDao,
            )
        }

    @Test
    fun `Given default parameters, When toggleLiked is called, Then addLiked is called with current time`() =
        runTest {
            // Given
            val fixedInstant = Clock.System.now()
            coEvery { episodeDao.isLiked(any()) } returns flowOf(false)
            coEvery { episodeDao.addLiked(any()) } just Runs
            coEvery { episodeDao.removeLiked(any()) } just Runs

            // When
            dataSource.toggleLiked(episodeEntity.id, fixedInstant)

            // Then
            coVerifySequence {
                episodeDao.isLiked(episodeEntity.id)
                episodeDao.addLiked(match { it.id == episodeEntity.id && it.likedAt == fixedInstant })
            }
            confirmVerified(
                episodeDao,
            )
        }

    @Test
    fun `Given isLiked return false, When toggleLiked is called, Then addLiked is called`() =
        runTest {
            // Given
            coEvery { episodeDao.isLiked(any()) } returns flowOf(false)
            coEvery { episodeDao.addLiked(any()) } just Runs
            coEvery { episodeDao.removeLiked(any()) } just Runs

            // When
            dataSource.toggleLiked(episodeEntity.id)

            // Then
            coVerifySequence {
                episodeDao.isLiked(episodeEntity.id)
                episodeDao.addLiked(any())
            }
            confirmVerified(
                episodeDao,
            )
        }

    @Test
    fun `Given isLiked return true, When toggleLiked is called, Then removeLiked is called`() =
        runTest {
            // Given
            coEvery { episodeDao.isLiked(any()) } returns flowOf(true)
            coEvery { episodeDao.addLiked(any()) } just Runs
            coEvery { episodeDao.removeLiked(any()) } just Runs

            // When
            dataSource.toggleLiked(episodeEntity.id)

            // Then
            coVerifySequence {
                episodeDao.isLiked(episodeEntity.id)
                episodeDao.removeLiked(episodeEntity.id)
            }
            confirmVerified(
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When upsertPlayed is called, Then upsertPlayed of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.upsertPlayed(any()) } just Runs

            // When
            dataSource.upsertPlayed(
                PlayedEpisodeEntity(
                    id = episodeEntity.id,
                    playedAt = Clock.System.now(),
                    position = 2000.seconds,
                )
            )

            // Then
            coVerify { episodeDao.upsertPlayed(any()) }
            confirmVerified(
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When removePlayed is called, Then removePlayed of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.removePlayed(any()) } just Runs

            // When
            dataSource.removePlayed(episodeEntity.id)

            // Then
            coVerify { episodeDao.removePlayed(episodeEntity.id) }
            confirmVerified(
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
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getPlayingEpisodes is called, Then getPlayingEpisodes of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getPlayingEpisodes() } returns mockk()

            // When
            dataSource.getPlayingEpisodes()

            // Then
            coVerify { episodeDao.getPlayingEpisodes() }
            confirmVerified(
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getPlayedEpisodes is called, Then getPlayedEpisodes of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getPlayedEpisodes() } returns mockk()

            // When
            dataSource.getPlayedEpisodes()

            // Then
            coVerify { episodeDao.getPlayedEpisodes() }
            confirmVerified(
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
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getPlayingEpisodeCount is called, Then getPlayingEpisodeCount of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getPlayingEpisodeCount() } returns mockk()

            // When
            dataSource.getPlayingEpisodeCount()

            // Then
            coVerify { episodeDao.getPlayingEpisodeCount() }
            confirmVerified(
                episodeDao,
            )
        }

    @Test
    fun `Given dependencies, When getPlayedEpisodeCount is called, Then getPlayedEpisodeCount of dao is called`() =
        runTest {
            // Given
            coEvery { episodeDao.getPlayedEpisodeCount() } returns mockk()

            // When
            dataSource.getPlayedEpisodeCount()

            // Then
            coVerify { episodeDao.getPlayedEpisodeCount() }
            confirmVerified(
                episodeDao,
            )
        }
}