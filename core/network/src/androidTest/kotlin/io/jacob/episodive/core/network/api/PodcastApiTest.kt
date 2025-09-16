package io.jacob.episodive.core.network.api

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PodcastApiTest {
    @get:Rule
    val retrofitRule = RetrofitRule()

    private lateinit var api: PodcastApi

    @Before
    fun setup() {
        api = retrofitRule.retrofit.create(PodcastApi::class.java)
    }

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
}