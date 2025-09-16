package io.jacob.episodive.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EpisodeApiTest {
    @get:Rule
    val retrofitRule = RetrofitRule()

    private lateinit var api: EpisodeApi

    @Before
    fun setup() {
        api = retrofitRule.retrofit.create(EpisodeApi::class.java)
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
        assertEquals(2, liveEpisodes?.size)
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
}