package io.jacob.episodive.core.data.util.query

import io.jacob.episodive.core.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 캐시 그룹 키의 계약을 고정한다.
 *
 * 이 키가 흔들리면 홈 미리보기와 '더 보기' 전체 목록이 같은 그룹을 덮어써서, 전체 목록이
 * 홈과 똑같은 개수만 보여주거나 반대로 홈이 필요 이상으로 큰 요청을 내게 된다. 어느 쪽도
 * 화면만 봐서는 원인을 알기 어려우므로 여기서 먼저 걸리게 한다.
 */
class PodcastQueryTest {

    @Test
    fun `Given same condition, when scope differs, then keys differ`() {
        // Given
        val preview = PodcastQuery.Trending(
            max = 10,
            language = "ko",
            categories = listOf(Category.BUSINESS),
            scope = QueryScope.PREVIEW,
        )
        val full = preview.copy(scope = QueryScope.FULL)

        // When & Then
        assertNotEquals(preview.key, full.key)
    }

    @Test
    fun `Given same condition, when max differs, then keys are equal`() {
        // max 는 원격에서 몇 개를 받을지일 뿐 어떤 목록인지를 가르지 않는다. 이게 키에 새면
        // 같은 목록이 max 값마다 별도 그룹으로 쌓여 캐시 상한을 헛되이 소진한다.
        // Given
        val small = PodcastQuery.Trending(max = 10, language = "ko")
        val large = small.copy(max = 100)

        // When & Then
        assertEquals(small.key, large.key)
    }

    @Test
    fun `Given trending query, when key split by colon, then prefix stays group name`() {
        // PodcastDao.replacePodcasts 가 groupKey.split(":").first() 로 캐시 상한을 건다.
        // 스코프를 앞에 끼워 넣으면 그 계약이 깨진다.
        // Given
        val query = PodcastQuery.Trending(
            max = 10,
            language = "ko",
            categories = listOf(Category.BUSINESS),
            scope = QueryScope.FULL,
        )

        // When & Then
        assertEquals("trending", query.key.split(":").first())
    }

    @Test
    fun `Given recent query, when key split by colon, then prefix stays group name`() {
        // Given
        val query = PodcastQuery.Recent(max = 10, scope = QueryScope.FULL)

        // When & Then
        assertEquals("recent", query.key.split(":").first())
    }

    @Test
    fun `Given no scope given, when key read, then defaults to preview`() {
        // 기본값이 PREVIEW 라서 기존 홈 경로가 그대로 동작한다. 이게 바뀌면 홈이 전체 목록
        // 그룹을 덮어쓴다.
        // Given
        val query = PodcastQuery.Trending(max = 10, language = "ko")

        // When & Then
        assertTrue(query.key.startsWith("trending:preview:"))
    }

    @Test
    fun `Given different categories, when keys built, then keys differ`() {
        // Given
        val business = PodcastQuery.Trending(
            max = 10,
            language = "ko",
            categories = listOf(Category.BUSINESS),
        )
        val comedy = business.copy(categories = listOf(Category.COMEDY))

        // When & Then
        assertNotEquals(business.key, comedy.key)
    }

    @Test
    fun `Given empty categories, when key built, then language scope is still distinguishable`() {
        // '국내 인기' 는 카테고리 없이 언어만으로 조회한다. 카테고리가 있는 '내 트렌딩' 과
        // 같은 그룹이 되면 안 된다.
        // Given
        val languageOnly = PodcastQuery.Trending(max = 10, language = "ko")
        val withCategories = languageOnly.copy(categories = listOf(Category.BUSINESS))

        // When & Then
        assertNotEquals(languageOnly.key, withCategories.key)
    }
}
