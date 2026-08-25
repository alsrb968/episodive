package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.common.ApplicationScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캐시 갱신을 화면 밖에서, 키마다 하나씩만 돌린다.
 *
 * 두 가지를 함께 지킨다.
 *
 * **화면보다 오래 산다.** [ApplicationScope] 에서 돌기 때문에 사용자가 탭을 옮겨도 갱신이
 * 끝까지 갑니다. 이것이 없으면 응답이 느린 요청은 `WhileSubscribed(5_000)` 이 걷히는 5초 뒤
 * 매번 취소되어, 다음에 들어와도 여전히 낡은 캐시를 보게 된다 — 갱신은 매번 시작만 하고
 * 아무것도 남기지 못한다. 캐시가 아예 없는 경우에는 더 나빠서, 탭을 오가는 동안 **영영 첫
 * 데이터를 받지 못한 채** 스켈레톤에 갇힌다.
 *
 * **같은 키는 하나만 돈다.** [RemoteUpdater] 는 요청마다 새로 만들어지는 데다 화면을
 * 드나들 때마다 다시 구독되므로, 막지 않으면 아직 끝나지 않은 갱신 위로 같은 요청이
 * 계속 쌓인다. 화면을 막지 않게 된 뒤로는 그 중복이 사용자 눈에 보이지도 않아 더 위험하다.
 */
@Singleton
class BackgroundRefresher @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val inFlight = ConcurrentHashMap<String, CompletableDeferred<Unit>>()

    /**
     * 결과를 기다리지 않는 갱신. 보여줄 캐시가 이미 있을 때 쓴다.
     */
    fun refreshInBackground(key: String, block: suspend () -> Unit) {
        val job = launchOnce(key, block)

        // 아무도 기다리지 않는 실패는 Deferred 안에 갇힌 채 남는다. 여기서 소비해 로그로 뺀다.
        scope.launch {
            try {
                job.await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // 보여줄 캐시가 이미 있어서 뒤에서 도는 갱신이다. 실패해도 화면에 올릴 것이
                // 없으므로 기록만 남기고 기존 캐시를 그대로 둔다.
                Timber.e(e, "백그라운드 캐시 갱신에 실패해 기존 캐시를 유지한다 (key=$key)")
            }
        }
    }

    /**
     * 갱신이 끝날 때까지 기다리고, 실패는 그대로 올린다. 보여줄 캐시가 없어 기다리는 것 말고
     * 할 일이 없을 때 쓴다.
     *
     * **기다리는 쪽이 취소돼도 갱신은 완주한다.** 대기만 끊기고 요청은 [ApplicationScope]
     * 에서 계속 돌아 캐시에 남으므로, 다음에 들어오면 이미 준비돼 있다.
     */
    suspend fun refreshAndWait(key: String, block: suspend () -> Unit) {
        launchOnce(key, block).await()
    }

    /**
     * 키마다 하나만 띄운다. 이미 돌고 있으면 그 작업을 그대로 돌려주므로, 기다리는 쪽이
     * 둘이어도 원격 요청은 하나다.
     *
     * 자리를 [CompletableDeferred] 로 **먼저** 잡고 작업은 그 뒤에 띄운다. 코루틴을 만드는
     * 도중에 맵을 다시 건드리지 않기 위해서다.
     */
    private fun launchOnce(key: String, block: suspend () -> Unit): Deferred<Unit> {
        val gate = CompletableDeferred<Unit>()
        inFlight.putIfAbsent(key, gate)?.let { return it }

        // ATOMIC 이라야 한다. 기본값(DEFAULT)이면 첫 디스패치 전에 취소됐을 때 본문이 아예
        // 시작되지 않아 finally 가 돌지 않고, 그 키는 앱이 살아 있는 동안 영영 막힌 채로
        // 남는다. SWR 은 늘 낡은 캐시를 흘려보내므로 화면이 멈춘 것처럼 보이지도 않아
        // 눈치채기 어렵다.
        scope.launch(start = CoroutineStart.ATOMIC) {
            try {
                block()
                gate.complete(Unit)
            } catch (e: Throwable) {
                gate.completeExceptionally(e)
            } finally {
                inFlight.remove(key, gate)
            }
        }

        return gate
    }
}
