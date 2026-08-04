package io.jacob.episodive.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 정규화가 고쳐야 할 것만 고치고 나머지는 건드리지 않는지 고정한다.
 *
 * 이 함수의 위험은 "404를 못 고치는 것"이 아니라 "멀쩡한 URL을 깨뜨리는 것"이므로,
 * 불변이어야 하는 케이스를 고치는 케이스보다 많이 둔다.
 */
class ImageUrlNormalizerTest {

    @Test
    fun `Given doubled slash in path, when normalized, then collapses to one`() {
        assertEquals(
            "http://graphics.nytimes.com/images/apps/podcasts/w750/theethicist.jpg",
            normalizeImageUrl("http://graphics.nytimes.com//images/apps/podcasts/w750/theethicist.jpg"),
        )
    }

    @Test
    fun `Given multiple doubled slashes, when normalized, then collapses all`() {
        assertEquals("https://h/a/b/c.png", normalizeImageUrl("https://h/a//b//c.png"))
    }

    @Test
    fun `Given tripled slash, when normalized, then collapses to one`() {
        assertEquals("https://h/a/b.png", normalizeImageUrl("https://h/a///b.png"))
    }

    @Test
    fun `Given trailing slash, when normalized, then keeps it`() {
        // 끝 슬래시를 지우면 서버가 리다이렉트하거나 404를 낸다. 중복이 아니라 의미다.
        assertEquals("https://h/a/", normalizeImageUrl("https://h/a/"))
    }

    @Test
    fun `Given doubled slash with trailing slash, when normalized, then collapses but keeps trailing`() {
        assertEquals("https://h/a/b/", normalizeImageUrl("https://h/a//b/"))
    }

    @Test
    fun `Given root path only, when normalized, then unchanged`() {
        assertEquals("https://h/", normalizeImageUrl("https://h/"))
    }

    @Test
    fun `Given normal url, when normalized, then unchanged`() {
        assertEquals("https://h/a/b.jpg", normalizeImageUrl("https://h/a/b.jpg"))
    }

    @Test
    fun `Given doubled slash in query, when normalized, then query untouched`() {
        assertEquals("https://h/a/b.jpg?x=//y", normalizeImageUrl("https://h/a/b.jpg?x=//y"))
    }

    @Test
    fun `Given doubled slash in fragment, when normalized, then fragment untouched`() {
        assertEquals("https://h/a/b.jpg#f//g", normalizeImageUrl("https://h/a/b.jpg#f//g"))
    }

    @Test
    fun `Given percent encoded slashes, when normalized, then untouched`() {
        // %2F 는 경로 구분자가 아니라 파일명의 일부다. 접으면 다른 파일을 가리킨다.
        assertEquals("https://h/a%2F%2Fb.jpg", normalizeImageUrl("https://h/a%2F%2Fb.jpg"))
    }

    @Test
    fun `Given path fixed with query present, when normalized, then query preserved`() {
        assertEquals(
            "https://h/images/a.jpg?aid=rss_feed",
            normalizeImageUrl("https://h//images/a.jpg?aid=rss_feed"),
        )
    }

    @Test
    fun `Given non http scheme, when normalized, then returned as is`() {
        assertEquals("content://media/1", normalizeImageUrl("content://media/1"))
        assertEquals("file:///x//y", normalizeImageUrl("file:///x//y"))
        assertEquals("android.resource://pkg/123", normalizeImageUrl("android.resource://pkg/123"))
    }

    @Test
    fun `Given blank input, when normalized, then returned as is`() {
        assertEquals("", normalizeImageUrl(""))
        assertEquals("   ", normalizeImageUrl("   "))
    }

    @Test
    fun `Given userinfo and port, when normalized, then preserved`() {
        assertEquals("https://user:pw@h:8443/a", normalizeImageUrl("https://user:pw@h:8443//a"))
    }
}
