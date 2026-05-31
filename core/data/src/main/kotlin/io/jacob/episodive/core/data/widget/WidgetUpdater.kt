package io.jacob.episodive.core.data.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

sealed interface WidgetUpdateRequest {
    data object NowPlayingChanged : WidgetUpdateRequest
    data object RecentEpisodesChanged : WidgetUpdateRequest
    data object All : WidgetUpdateRequest
}

@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(EpisodiveDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    private val widgetDispatcher: WidgetDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    internal val events = MutableSharedFlow<WidgetUpdateRequest>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    // distinctUntilChanged 금지: 요청 타입은 같아도(NowPlayingChanged data object)
    // 가리키는 에피소드 데이터는 매번 달라지므로, 연속 변경을 드롭하면 위젯이 갱신되지 않는다.
    // 폭주 방지는 conflate + debounce 로 충분.
    @OptIn(FlowPreview::class)
    private val pipeline = events
        .conflate()
        .debounce(DEBOUNCE_MS)
        .onEach { req -> widgetDispatcher.dispatch(context, req) }
        .launchIn(scope)

    fun notifyNowPlayingChanged() {
        events.tryEmit(WidgetUpdateRequest.NowPlayingChanged)
    }

    fun notifyRecentEpisodesChanged() {
        events.tryEmit(WidgetUpdateRequest.RecentEpisodesChanged)
    }

    fun notifyAllWidgets() {
        events.tryEmit(WidgetUpdateRequest.All)
    }

    companion object {
        internal const val DEBOUNCE_MS = 250L
    }
}
