package io.jacob.episodive.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PodcastIndexApiTest {
    private val api = PodcastIndexClient.retrofit.create(PodcastIndexApi::class.java)

    @Test
    fun searchPodcastsTest() = runTest {
        val response = api.searchPodcasts(
            query = "슈카월드",
        )
        val podcast = response.dataList.first()

        assertNotNull(response)
        assertNotNull(podcast)
        assertEquals(1, response.dataList.size)
        assertEquals(5778530, podcast.id)
        assertEquals("슈카월드", podcast.title)
        assertEquals(mapOf(9 to "Business"), podcast.categories)
        assertEquals("podcast", podcast.medium)
    }

    @Test
    fun searchEpisodesByPersonTest() = runTest {
        val response = api.searchEpisodesByPerson(
            person = "adam curry",
            max = 10,
        )
        val episode = response.dataList.first()

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(10, response.dataList.size)
        assertTrue("Adam Curry".contains(episode.feedAuthor!!))
    }

    @Test
    fun getPodcastByFeedIdTest() = runTest {
        val response = api.getPodcastByFeedId(
            feedId = 5778530
        )
        val podcast = response.data

        assertNotNull(response)
        assertNotNull(podcast)
        assertEquals(5778530, podcast.id)
        assertEquals("슈카월드", podcast.title)
        assertEquals(mapOf(9 to "Business"), podcast.categories)
        assertEquals("podcast", podcast.medium)
    }

    @Test
    fun getPodcastByFeedUrlTest() = runTest {
        val response = api.getPodcastByFeedUrl(
            feedUrl = "https://anchor.fm/s/ddaceaa8/podcast/rss"
        )
        val podcast = response.data

        assertNotNull(response)
        assertNotNull(podcast)
        assertEquals(5778530, podcast.id)
        assertEquals("슈카월드", podcast.title)
        assertEquals(mapOf(9 to "Business"), podcast.categories)
        assertEquals("podcast", podcast.medium)
    }

    @Test
    fun getPodcastByGuidTest() = runTest {
        val response = api.getPodcastByGuid(
            guid = "7eddd03e-20ae-5a29-ae59-a1a9c8091322"
        )
        val podcast = response.data

        assertNotNull(response)
        assertNotNull(podcast)
        assertEquals(5778530, podcast.id)
        assertEquals("슈카월드", podcast.title)
        assertEquals(mapOf(9 to "Business"), podcast.categories)
        assertEquals("podcast", podcast.medium)
    }

    @Test
    fun getPodcastsByMediumTest() = runTest {
        val response = api.getPodcastsByMedium(
            medium = "video",
            max = 10,
        )

        assertNotNull(response)
        assertEquals(10000, response.dataList.size) // server error
    }

    @Test
    fun getTrendingPodcastsTest() = runTest {
        val response = api.getTrendingPodcasts(
            max = 10
        )

        assertNotNull(response)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getEpisodesByFeedIdTest() = runTest {
        val response = api.getEpisodesByFeedId(
            feedId = 7156165,
            max = 10,
        )
        val liveEpisodes = response.liveEpisodes
        val episodes = response.dataList

        assertNotNull(response)
        assertNotNull(liveEpisodes)
        assertNotNull(episodes)
        assertEquals(1, liveEpisodes?.size)
        assertEquals(10, episodes.size)
        assertEquals(7156165L, liveEpisodes?.first()?.feedId)
        assertEquals(7156165L, episodes.first().feedId)
    }

    @Test
    fun getEpisodesByFeedUrlTest() = runTest {
        val response = api.getEpisodesByFeedUrl(
            feedUrl = "https://anchor.fm/s/ddaceaa8/podcast/rss",
            max = 10,
        )
        val episode = response.dataList.first()

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(10, response.dataList.size)
        assertEquals(5778530, episode.feedId)
    }

    @Test
    fun getEpisodesByPodcastGuidTest() = runTest {
        val response = api.getEpisodesByPodcastGuid(
            guid = "7eddd03e-20ae-5a29-ae59-a1a9c8091322",
            max = 10,
        )
        val episode = response.dataList.first()

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(10, response.dataList.size)
        assertEquals(5778530, episode.feedId)
    }

    @Test
    fun getEpisodeByIdTest() = runTest {
        val response = api.getEpisodeById(
            id = 42340708554
        )
        val episode = response.data

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(5778530, episode.feedId)
    }

    @Test
    fun getLiveEpisodesTest() = runTest {
        val response = api.getLiveEpisodes(
            max = 10,
        )
        val episode = response.dataList.first()

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getRandomEpisodesTest() = runTest {
        val response = api.getRandomEpisodes(
            max = 10,
        )
        val episode = response.dataList.first()

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getRecentEpisodesTest() = runTest {
        val response = api.getRecentEpisodes(
            max = 10,
        )
        val episode = response.dataList.first()

        assertNotNull(response)
        assertNotNull(episode)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getRecentFeedsTest() = runTest {
        val response = api.getRecentFeeds(
            max = 10,
        )
        val feed = response.dataList.first()

        assertNotNull(response)
        assertNotNull(feed)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getRecentNewFeedsTest() = runTest {
        val response = api.getRecentNewFeeds(
            max = 10,
        )
        val feed = response.dataList.first()

        assertNotNull(response)
        assertNotNull(feed)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getRecentNewValueFeedsTest() = runTest {
        val response = api.getRecentNewValueFeeds(
            max = 10,
        )
        val feed = response.dataList.first()

        assertNotNull(response)
        assertNotNull(feed)
        assertEquals(10, response.dataList.size)
    }

    @Test
    fun getRecentSoundbitesTest() = runTest {
        val response = api.getRecentSoundbites(
            max = 10,
        )
        val soundbite = response.dataList.first()

        assertNotNull(response)
        assertNotNull(soundbite)
        assertEquals(10, response.dataList.size)
    }
}