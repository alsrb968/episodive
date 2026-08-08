package io.jacob.episodive.core.data.util.query

import io.jacob.episodive.core.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 에피소드 캐시 그룹 키의 계약을 고정한다. 취지는 [PodcastQueryTest] 와 같다.
 */
class EpisodeQueryTest {

    @Test
    fun `Given live query, when scope differs, then keys differ`() {
        // Given
        val preview = EpisodeQuery.Live(max = 6, scope = QueryScope.PREVIEW)
        val full = preview.copy(scope = QueryScope.FULL)

        // When & Then
        assertNotEquals(preview.key, full.key)
    }

    @Test
    fun `Given random query, when scope differs, then keys differ`() {
        // Given
        val preview = EpisodeQuery.Random(max = 6, language = "ko", scope = QueryScope.PREVIEW)
        val full = preview.copy(scope = QueryScope.FULL)

        // When & Then
        assertNotEquals(preview.key, full.key)
    }

    @Test
    fun `Given random query, when categories differ, then keys differ`() {
        // 예전에는 키가 그룹 이름 하나로 고정돼 있어, 관심 카테고리를 바꿔도 이전 조건으로
        // 받아 둔 캐시를 계속 보게 됐다.
        // Given
        val business = EpisodeQuery.Random(max = 6, categories = listOf(Category.BUSINESS))
        val comedy = business.copy(categories = listOf(Category.COMEDY))

        // When & Then
        assertNotEquals(business.key, comedy.key)
    }

    @Test
    fun `Given random query, when language differs, then keys differ`() {
        // Given
        val korean = EpisodeQuery.Random(max = 6, language = "ko")
        val english = korean.copy(language = "en")

        // When & Then
        assertNotEquals(korean.key, english.key)
    }

    @Test
    fun `Given same condition, when max differs, then keys are equal`() {
        // Given
        val small = EpisodeQuery.Live(max = 6)
        val large = small.copy(max = 100)

        // When & Then
        assertEquals(small.key, large.key)
    }

    @Test
    fun `Given episode queries, when key split by colon, then prefix stays group name`() {
        // EpisodeDao 도 groupKey.split(":").first() 로 캐시 상한을 건다.
        // When & Then
        assertEquals("live", EpisodeQuery.Live(max = 6, scope = QueryScope.FULL).key.split(":").first())
        assertEquals("random", EpisodeQuery.Random(max = 6, scope = QueryScope.FULL).key.split(":").first())
    }

    @Test
    fun `Given no scope given, when key read, then defaults to preview`() {
        // When & Then
        assertTrue(EpisodeQuery.Live(max = 6).key.startsWith("live:preview"))
        assertTrue(EpisodeQuery.Random(max = 6).key.startsWith("random:preview:"))
    }
}
