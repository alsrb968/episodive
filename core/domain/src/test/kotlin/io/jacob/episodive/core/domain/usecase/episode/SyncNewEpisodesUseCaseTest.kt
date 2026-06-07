package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class SyncNewEpisodesUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastRepository = mockk<PodcastRepository>(relaxed = true)
    private val episodeRepository = mockk<EpisodeRepository>(relaxed = true)

    private val useCase = SyncNewEpisodesUseCase(
        podcastRepository = podcastRepository,
        episodeRepository = episodeRepository,
    )

    @Test
    fun `Given no followed podcasts, when invoke, then returns empty list`() = runTest {
        coEvery { podcastRepository.getFollowedPodcastsToSync() } returns emptyMap()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given followed podcast with cached episodes, when invoke, then syncs since latest cached date`() = runTest {
        val feedId = 5778530L
        val followedAt = Instant.fromEpochSeconds(1757700000)
        val since = Instant.fromEpochSeconds(1757797200)
        val newEpisodes = episodeTestDataList.take(2)

        coEvery { podcastRepository.getFollowedPodcastsToSync() } returns mapOf(feedId to followedAt)
        coEvery { episodeRepository.getLatestEpisodeDatePublished(feedId) } returns since
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId, since) } returns newEpisodes

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(feedId, result[0].feedId)
        assertEquals(2, result[0].episodes.size)
        coVerify(exactly = 1) { episodeRepository.fetchAndSaveNewEpisodes(feedId, since) }
    }

    @Test
    fun `Given followed podcast with no cached episodes, when invoke, then falls back to followedAt`() = runTest {
        val feedId = 5778530L
        val followedAt = Instant.fromEpochSeconds(1757700000)
        val newEpisodes = episodeTestDataList.take(2)

        coEvery { podcastRepository.getFollowedPodcastsToSync() } returns mapOf(feedId to followedAt)
        coEvery { episodeRepository.getLatestEpisodeDatePublished(feedId) } returns null
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId, followedAt) } returns newEpisodes

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(feedId, result[0].feedId)
        coVerify(exactly = 1) { episodeRepository.fetchAndSaveNewEpisodes(feedId, followedAt) }
    }

    @Test
    fun `Given followed podcast with no new episodes, when invoke, then returns empty`() = runTest {
        val feedId = 5778530L
        val followedAt = Instant.fromEpochSeconds(1757700000)
        val since = Instant.fromEpochSeconds(1757797200)

        coEvery { podcastRepository.getFollowedPodcastsToSync() } returns mapOf(feedId to followedAt)
        coEvery { episodeRepository.getLatestEpisodeDatePublished(feedId) } returns since
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId, since) } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given network error for one feed, when invoke, then continues with others`() = runTest {
        val feedId1 = 5778530L
        val feedId2 = 9999999L
        val followedAt1 = Instant.fromEpochSeconds(1757600000)
        val followedAt2 = Instant.fromEpochSeconds(1757650000)
        val since1 = Instant.fromEpochSeconds(1757797200)
        val since2 = Instant.fromEpochSeconds(1757883600)
        val newEpisodes = episodeTestDataList.take(1)

        coEvery { podcastRepository.getFollowedPodcastsToSync() } returns
            mapOf(feedId1 to followedAt1, feedId2 to followedAt2)
        coEvery { episodeRepository.getLatestEpisodeDatePublished(feedId1) } returns since1
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId1, since1) } throws RuntimeException("Network error")
        coEvery { episodeRepository.getLatestEpisodeDatePublished(feedId2) } returns since2
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId2, since2) } returns newEpisodes

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(feedId2, result[0].feedId)
    }

    @Test
    fun `Given multiple followed podcasts, when invoke, then syncs all of them`() = runTest {
        val feedId1 = 5778530L
        val feedId2 = 9999999L
        val followedAt = Instant.fromEpochSeconds(1757600000)
        val since = Instant.fromEpochSeconds(1757797200)
        val newEpisodes = episodeTestDataList.take(1)

        coEvery { podcastRepository.getFollowedPodcastsToSync() } returns
            mapOf(feedId1 to followedAt, feedId2 to followedAt)
        coEvery { episodeRepository.getLatestEpisodeDatePublished(any()) } returns since
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId1, since) } returns newEpisodes
        coEvery { episodeRepository.fetchAndSaveNewEpisodes(feedId2, since) } returns newEpisodes

        val result = useCase()

        assertEquals(2, result.size)
        coVerify(exactly = 1) { podcastRepository.getFollowedPodcastsToSync() }
    }
}
