package io.jacob.episodive.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.jacob.episodive.feature.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

/**
 * [autoFocus] 는 라우트 인자가 아니라 앱이 들고 있는 신호다. [SearchRoute] 는 탭 루트라
 * 백스택 키로도 쓰이므로, 값을 실어 나르는 순간 같은 탭이 서로 다른 키가 되어 스택 매칭이
 * 깨진다. 그래서 키는 그대로 두고 신호만 따로 내려보낸다.
 *
 * **`Boolean` 이 아니라 읽는 함수인 것이 핵심이다.** navigation3 는 백스택이 그대로면
 * `NavEntry` 를 재사용하므로, 아래 `entry { }` 블록은 **처음 만들어질 때 캡처한 값**을 계속
 * 쓴다. 값을 그대로 받으면 나중에 신호가 켜져도 이 화면은 영영 `false` 만 본다 — 실제로
 * 겪은 버그다. 함수로 받아 컴포지션 안에서 읽으면 최신 값이 오고 스냅샷 구독도 걸린다.
 */
fun EntryProviderScope<NavKey>.searchEntries(
    onPodcastClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    autoFocus: () -> Boolean = { false },
    onAutoFocusHandled: () -> Unit = {},
) {
    entry<SearchRoute> {
        SearchRoute(
            onPodcastClick = onPodcastClick,
            onShowSnackbar = onShowSnackbar,
            autoFocus = autoFocus(),
            onAutoFocusHandled = onAutoFocusHandled,
        )
    }
}
