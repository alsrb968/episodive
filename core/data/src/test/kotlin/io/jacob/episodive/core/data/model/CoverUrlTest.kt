package io.jacob.episodive.core.data.model

import io.jacob.episodive.core.model.coverUrl
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.model.podcastTestData
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 커버 URL 폴백 규칙을 고정한다.
 *
 * 이 규칙이 흔들리면 화면마다 커버가 떴다 안 떴다 하므로, 순서를 바꾸는 변경은 여기서 먼저 걸린다.
 */
class CoverUrlTest {

    @Test
    fun `Given podcast with image, when coverUrl read, then returns image`() {
        // Given
        val podcast = podcastTestData.copy(
            image = "https://example.com/image.jpg",
            artwork = "https://example.com/artwork.jpg",
        )

        // When & Then
        assertEquals("https://example.com/image.jpg", podcast.coverUrl)
    }

    @Test
    fun `Given podcast with blank image, when coverUrl read, then falls back to artwork`() {
        // Given
        val podcast = podcastTestData.copy(
            image = "",
            artwork = "https://example.com/artwork.jpg",
        )

        // When & Then
        assertEquals("https://example.com/artwork.jpg", podcast.coverUrl)
    }

    @Test
    fun `Given podcast with both blank, when coverUrl read, then returns blank`() {
        // Given
        val podcast = podcastTestData.copy(image = "", artwork = "")

        // When & Then
        assertEquals("", podcast.coverUrl)
    }

    @Test
    fun `Given episode with image, when coverUrl read, then returns image`() {
        // Given
        val episode = episodeTestData.copy(
            image = "https://example.com/episode.jpg",
            feedImage = "https://example.com/feed.jpg",
        )

        // When & Then
        assertEquals("https://example.com/episode.jpg", episode.coverUrl)
    }

    @Test
    fun `Given episode with blank image, when coverUrl read, then falls back to feedImage`() {
        // Given
        val episode = episodeTestData.copy(
            image = "",
            feedImage = "https://example.com/feed.jpg",
        )

        // When & Then
        assertEquals("https://example.com/feed.jpg", episode.coverUrl)
    }

    @Test
    fun `Given episode with both blank, when coverUrl read, then returns blank`() {
        // Given
        val episode = episodeTestData.copy(image = "", feedImage = "")

        // When & Then
        assertEquals("", episode.coverUrl)
    }

    @Test
    fun `Given whitespace only image, when coverUrl read, then treats as blank`() {
        // Given — API 가 공백만 있는 문자열을 주는 경우가 있어 isEmpty 가 아니라 isBlank 로 판정한다
        val episode = episodeTestData.copy(
            image = "   ",
            feedImage = "https://example.com/feed.jpg",
        )

        // When & Then
        assertEquals("https://example.com/feed.jpg", episode.coverUrl)
    }
}
