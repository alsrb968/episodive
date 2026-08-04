package io.jacob.episodive.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageFailureCacheTest {

    private var now = 0L
    private val cache = ImageFailureCache { now }

    @Test
    fun `Given unrecorded url, when checked, then not blocked`() {
        assertFalse(cache.isBlocked("https://example.com/a.jpg"))
    }

    @Test
    fun `Given recorded url, when checked within ttl, then blocked`() {
        // Given
        cache.record(URL, ttlMs = 1_000)

        // When
        now = 999

        // Then
        assertTrue(cache.isBlocked(URL))
    }

    @Test
    fun `Given recorded url, when ttl expired, then not blocked`() {
        // Given
        cache.record(URL, ttlMs = 1_000)

        // When
        now = 1_000

        // Then
        assertFalse(cache.isBlocked(URL))
    }

    @Test
    fun `Given expired entry, when checked, then evicted`() {
        // Given
        cache.record(URL, ttlMs = 1_000)
        now = 2_000

        // When
        cache.isBlocked(URL)

        // Then — 만료된 항목이 자리를 계속 차지하지 않는다
        assertEquals(0, cache.size())
    }

    @Test
    fun `Given recorded url, when cleared, then not blocked`() {
        // Given
        cache.record(URL, ttlMs = 10_000)

        // When
        cache.clear(URL)

        // Then — 서버가 고쳐졌는데 TTL 때문에 계속 막히면 안 된다
        assertFalse(cache.isBlocked(URL))
    }

    @Test
    fun `Given entries beyond capacity, when recorded, then oldest evicted`() {
        // Given & When
        repeat(300) { cache.record("https://example.com/$it.jpg", ttlMs = 10_000) }

        // Then
        assertEquals(256, cache.size())
        assertFalse(cache.isBlocked("https://example.com/0.jpg"))
        assertTrue(cache.isBlocked("https://example.com/299.jpg"))
    }

    @Test
    fun `Given concurrent records, when done, then stays within capacity`() {
        // Given & When
        val threads = List(8) { t ->
            Thread { repeat(100) { cache.record("https://example.com/$t-$it.jpg", ttlMs = 10_000) } }
        }
        threads.forEach(Thread::start)
        threads.forEach(Thread::join)

        // Then — 인터셉터는 여러 스레드에서 동시에 돈다
        assertEquals(256, cache.size())
    }

    private companion object {
        const val URL = "https://example.com/a.jpg"
    }
}
