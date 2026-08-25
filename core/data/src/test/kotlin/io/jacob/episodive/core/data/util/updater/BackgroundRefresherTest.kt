package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BackgroundRefresherTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `Given a refresh in flight, When the same key is asked again, Then it runs once`() =
        runTest {
            // 갱신이 화면을 막지 않게 된 뒤로 중복은 사용자 눈에 보이지 않는다. 막지 않으면
            // 느린 요청 하나가 도는 동안 탭을 드나드는 것만으로 같은 요청이 계속 쌓인다.
            val refresher = BackgroundRefresher(CoroutineScope(StandardTestDispatcher(testScheduler)))
            val hold = CompletableDeferred<Unit>()
            var runs = 0

            refresher.refresh("random") {
                runs++
                hold.await()
            }
            refresher.refresh("random") { runs++ }
            advanceUntilIdle()

            assertEquals(1, runs)

            // 앞선 갱신이 끝나면 다시 돌 수 있어야 한다.
            hold.complete(Unit)
            advanceUntilIdle()
            refresher.refresh("random") { runs++ }
            advanceUntilIdle()

            assertEquals(2, runs)
        }

    @Test
    fun `Given different keys, When asked together, Then both run`() = runTest {
        // 막는 단위는 키다. 한 섹션의 갱신이 다른 섹션을 붙잡으면 안 된다.
        val refresher = BackgroundRefresher(CoroutineScope(StandardTestDispatcher(testScheduler)))
        val ran = mutableListOf<String>()

        refresher.refresh("random") { ran += "random" }
        refresher.refresh("trending") { ran += "trending" }
        advanceUntilIdle()

        // 도는 순서는 계약이 아니다 — 둘 다 돌았다는 것만 본다.
        assertEquals(setOf("random", "trending"), ran.toSet())
    }

    @Test
    fun `Given a refresh that fails, When the same key is asked again, Then it runs again`() =
        runTest {
            // 실패한 키를 정리하지 않으면 그 섹션은 앱이 살아 있는 동안 영영 갱신되지 않는다.
            // 화면에는 오래된 캐시가 그대로 남아 아무 신호도 주지 않으므로 눈치채기 어렵다.
            val refresher = BackgroundRefresher(CoroutineScope(StandardTestDispatcher(testScheduler)))
            var runs = 0

            refresher.refresh("random") {
                runs++
                throw RuntimeException("boom")
            }
            advanceUntilIdle()

            refresher.refresh("random") { runs++ }
            advanceUntilIdle()

            assertEquals(2, runs)
        }
}
