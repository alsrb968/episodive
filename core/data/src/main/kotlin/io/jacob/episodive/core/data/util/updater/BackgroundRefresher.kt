package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.common.ApplicationScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캐시 갱신을 화면 뒤에서, 키마다 하나씩만 돌린다.
 *
 * 두 가지를 함께 지킨다.
 *
 * **화면보다 오래 산다.** [ApplicationScope] 에서 돌기 때문에 사용자가 탭을 옮겨도 갱신이
 * 끝까지 간다. 이것이 없으면 응답이 느린 요청은 `WhileSubscribed(5_000)` 이 걷히는 5초 뒤
 * 매번 취소되어, 다음에 들어와도 여전히 낡은 캐시를 보게 된다 — 갱신은 매번 시작만 하고
 * 아무것도 남기지 못한다.
 *
 * **같은 키는 하나만 돈다.** [RemoteUpdater] 는 요청마다 새로 만들어지는 데다 화면을
 * 드나들 때마다 다시 구독되므로, 막지 않으면 아직 끝나지 않은 갱신 위로 같은 요청이
 * 계속 쌓인다. 화면을 막지 않게 된 뒤로는 그 중복이 사용자 눈에 보이지도 않아 더 위험하다.
 */
@Singleton
class BackgroundRefresher @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val inFlight = ConcurrentHashMap.newKeySet<String>()

    fun refresh(key: String, block: suspend () -> Unit) {
        // add 가 false 면 같은 키가 이미 돌고 있다. 새로 띄우지 않는다.
        if (!inFlight.add(key)) return

        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // 보여줄 캐시가 이미 있어서 뒤에서 도는 갱신이다. 실패해도 화면에 올릴 것이
                // 없으므로 기록만 남기고 기존 캐시를 그대로 둔다.
                Timber.e(e, "백그라운드 캐시 갱신에 실패해 기존 캐시를 유지한다 (key=$key)")
            } finally {
                inFlight.remove(key)
            }
        }
    }
}
