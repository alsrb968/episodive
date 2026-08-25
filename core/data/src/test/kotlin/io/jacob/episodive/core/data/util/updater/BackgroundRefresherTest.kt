package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

            refresher.refreshInBackground("random") {
                runs++
                hold.await()
            }
            refresher.refreshInBackground("random") { runs++ }
            advanceUntilIdle()

            assertEquals(1, runs)

            // 앞선 갱신이 끝나면 다시 돌 수 있어야 한다.
            hold.complete(Unit)
            advanceUntilIdle()
            refresher.refreshInBackground("random") { runs++ }
            advanceUntilIdle()

            assertEquals(2, runs)
        }

    @Test
    fun `Given different keys, When asked together, Then both run`() = runTest {
        // 막는 단위는 키다. 한 섹션의 갱신이 다른 섹션을 붙잡으면 안 된다.
        val refresher = BackgroundRefresher(CoroutineScope(StandardTestDispatcher(testScheduler)))
        val ran = mutableListOf<String>()

        refresher.refreshInBackground("random") { ran += "random" }
        refresher.refreshInBackground("trending") { ran += "trending" }
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

            refresher.refreshInBackground("random") {
                runs++
                throw RuntimeException("boom")
            }
            advanceUntilIdle()

            refresher.refreshInBackground("random") { runs++ }
            advanceUntilIdle()

            assertEquals(2, runs)
        }

    @Test
    fun `Given the awaiter is cancelled, When it goes away, Then the refresh still finishes`() =
        runTest {
            // 이 클래스가 앱 스코프에서 도는 이유. 캐시가 없어 기다리는 쪽(화면)이 사라져도
            // — 홈을 떠나 WhileSubscribed 가 걷혀도 — 요청은 완주해 캐시에 남아야 한다.
            // 그러지 않으면 탭을 오갈 때마다 처음부터 다시 시작해 영영 첫 데이터를 못 받는다.
            val refresher = BackgroundRefresher(CoroutineScope(StandardTestDispatcher(testScheduler)))
            val hold = CompletableDeferred<Unit>()
            var finished = false

            val waiter = launch {
                refresher.refreshAndWait("random") {
                    hold.await()
                    finished = true
                }
            }
            advanceUntilIdle()

            waiter.cancel()
            advanceUntilIdle()
            assertEquals(false, finished)

            hold.complete(Unit)
            advanceUntilIdle()

            assertEquals(true, finished)
        }

    @Test
    fun `Given a refresh in flight, When someone awaits the same key, Then it runs once`() =
        runTest {
            // 기다리는 쪽과 뒤에서 도는 쪽이 같은 키를 가리키면 요청은 하나여야 한다.
            val refresher = BackgroundRefresher(CoroutineScope(StandardTestDispatcher(testScheduler)))
            val hold = CompletableDeferred<Unit>()
            var runs = 0

            refresher.refreshInBackground("random") {
                runs++
                hold.await()
            }
            advanceUntilIdle()

            val waiter = launch { refresher.refreshAndWait("random") { runs++ } }
            advanceUntilIdle()

            assertEquals(1, runs)

            hold.complete(Unit)
            advanceUntilIdle()
            waiter.join()

            assertEquals(1, runs)
        }
}
