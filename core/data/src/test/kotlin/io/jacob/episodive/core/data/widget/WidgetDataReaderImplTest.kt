package io.jacob.episodive.core.data.widget

import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.domain.usecase.player.GetNowPlayingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsUseCase
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class WidgetDataReaderImplTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNowPlaying = mockk<GetNowPlayingUseCase>(relaxed = true)
    private val getUserRecentPodcasts = mockk<GetUserRecentPodcastsUseCase>(relaxed = true)
    private val playerRepository = mockk<PlayerRepository>(relaxed = true)

    private val reader = WidgetDataReaderImpl(
        getNowPlaying = getNowPlaying,
        getUserRecentPodcasts = getUserRecentPodcasts,
        playerRepository = playerRepository,
    )

    @Test
    fun `Given no now playing episode, when snapshotNowPlaying called, then returns null`() =
        runTest {
            every { getNowPlaying() } returns flowOf(null)
            every { playerRepository.isPlaying } returns flowOf(false)

            val snapshot = reader.snapshotNowPlaying()

            assertNull(snapshot)
        }

    @Test
    fun `Given an episode and playing state, when snapshotNowPlaying called, then returns mapped snapshot`() =
        runTest {
            every { getNowPlaying() } returns flowOf(episodeTestData)
            every { playerRepository.isPlaying } returns flowOf(true)

            val snapshot = reader.snapshotNowPlaying()!!

            assertEquals(episodeTestData.id, snapshot.episodeId)
            assertEquals(episodeTestData.feedId, snapshot.podcastId)
            assertEquals(episodeTestData.title, snapshot.title)
            assertEquals(episodeTestData.feedTitle, snapshot.feedTitle)
            assertEquals(episodeTestData.image, snapshot.imageUrl)
            assertEquals(true, snapshot.isPlaying)
        }

    @Test
    fun `Given user recent podcasts, when userRecentPodcastsFlow collected, then maps to snapshots in order`() =
        runTest {
            val max = 3
            val sourcePodcasts = podcastTestDataList.take(max)
            every { getUserRecentPodcasts(max) } returns flowOf(sourcePodcasts)

            val snapshots = reader.userRecentPodcastsFlow(max).first()

            assertEquals(sourcePodcasts.size, snapshots.size)
            sourcePodcasts.forEachIndexed { index, podcast ->
                val snapshot = snapshots[index]
                assertEquals(podcast.id, snapshot.id)
                assertEquals(podcast.title, snapshot.title)
                assertEquals(
                    podcast.image.ifBlank { podcast.artwork }.ifBlank { null },
                    snapshot.imageUrl,
                )
            }
        }
}
