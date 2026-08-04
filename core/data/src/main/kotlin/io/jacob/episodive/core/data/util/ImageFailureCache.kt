package io.jacob.episodive.core.data.util

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 최근 실패한 이미지 URL을 TTL 동안 기억해 같은 요청이 반복되는 것을 막는다.
 *
 * Coil은 실패를 캐시하지 않아서, 커버가 없는 항목은 스크롤로 화면을 드나들 때마다 매번 네트워크를
 * 다시 탄다(실측: 같은 URL이 한 세션에 7회까지 재요청됐다). 결과는 늘 같은 실패인데 트래픽과 배터리만
 * 쓴다.
 *
 * **메모리 전용이고 영속화하지 않는다.** 잘못 기록된 항목이 앱 재시작을 넘어 살아남으면 사용자가
 * 취할 수 있는 유일한 복구 수단(앱을 다시 켜기)까지 막아버린다.
 */
@Singleton
class ImageFailureCache internal constructor(
    private val nowMs: () -> Long,
) {
    // Hilt 가 쓰는 생성자. 시계를 파라미터로 열어 두면 Dagger 가 Function0<Long> 바인딩을
    // 찾으려 들기 때문에, 주입 경로와 테스트 경로를 생성자로 갈라 둔다.
    @Inject
    constructor() : this(System::currentTimeMillis)

    private val entries = object : LinkedHashMap<String, Long>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>) = size > MAX_ENTRIES
    }

    private val lock = Any()

    /** [ttlMs] 동안 [url] 요청을 건너뛴다. */
    fun record(url: String, ttlMs: Long) {
        synchronized(lock) { entries[url] = nowMs() + ttlMs }
    }

    /** 아직 TTL이 남아 있으면 true. 만료된 항목은 조회하는 김에 걷어낸다. */
    fun isBlocked(url: String): Boolean = synchronized(lock) {
        val expiresAt = entries[url] ?: return false
        if (nowMs() < expiresAt) return true
        entries.remove(url)
        false
    }

    /** 성공했으면 즉시 지운다 — 서버가 고쳐졌는데 TTL 때문에 계속 막는 일이 없도록. */
    fun clear(url: String) {
        synchronized(lock) { entries.remove(url) }
    }

    fun clear() {
        synchronized(lock) { entries.clear() }
    }

    internal fun size(): Int = synchronized(lock) { entries.size }

    private companion object {
        // 한 화면에 뜨는 커버 수의 몇 배면 충분하다. 넘치면 오래된 것부터 버린다(LRU).
        const val MAX_ENTRIES = 256
        const val INITIAL_CAPACITY = 64
        const val LOAD_FACTOR = 0.75f
    }
}
