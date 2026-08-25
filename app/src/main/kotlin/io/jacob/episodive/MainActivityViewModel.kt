package io.jacob.episodive

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.user.GetUserDataUseCase
import io.jacob.episodive.core.model.share.EpisodiveDeepLink
import io.jacob.episodive.sync.EpisodeSyncNotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
) : ViewModel() {
    val state: StateFlow<MainActivityState> = getUserDataUseCase().map { userData ->
        if (userData.isFirstLaunch) {
            MainActivityState.FirstLaunch
        } else {
            MainActivityState.NotFirstLaunch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainActivityState.Loading,
    )

    private val _deepLinkEvent = MutableSharedFlow<DeepLinkEvent>(replay = 1)
    val deepLinkEvent: SharedFlow<DeepLinkEvent> = _deepLinkEvent.asSharedFlow()

    fun handleDeepLink(intent: Intent?) {
        intent ?: return

        // URI 를 먼저 본다. 사용자가 링크를 눌러 들어온 명시적 의도이고, 그런 Intent 에는
        // 아래 extra 들이 실리지 않아 순서를 다툴 일도 없다.
        EpisodiveDeepLink.parse(intent.data?.toString())?.let { link ->
            viewModelScope.launch { _deepLinkEvent.emit(link.toEvent()) }
            return
        }

        val podcastId = intent.getLongExtra(EpisodeSyncNotificationHelper.EXTRA_PODCAST_ID, -1L)
        if (podcastId > 0) {
            viewModelScope.launch { _deepLinkEvent.emit(DeepLinkEvent.Podcast(podcastId)) }
            return
        }
        // 위젯 now-playing 탭 → 현재(마지막) 재생 에피소드 플레이어 화면 펼치기.
        if (intent.getBooleanExtra(MainActivity.EXTRA_WIDGET_OPEN_PLAYER, false)) {
            // 액티비티 재생성 시 재방출(시트 재오픈) 방지를 위해 소비 후 제거.
            intent.removeExtra(MainActivity.EXTRA_WIDGET_OPEN_PLAYER)
            viewModelScope.launch { _deepLinkEvent.emit(DeepLinkEvent.Player) }
        }
    }

    fun consumeDeepLink() {
        _deepLinkEvent.resetReplayCache()
    }
}

/**
 * `episodive://` 링크를 앱 안의 사건으로 옮긴다. 팟캐스트는 이미 있는 [DeepLinkEvent.Podcast]
 * 를 그대로 쓰므로 알림 딥링크와 착지 경로를 공유한다.
 */
private fun EpisodiveDeepLink.toEvent(): DeepLinkEvent = when (this) {
    is EpisodiveDeepLink.Podcast -> DeepLinkEvent.Podcast(id)
    is EpisodiveDeepLink.Episode -> DeepLinkEvent.Episode(this)
}

sealed interface DeepLinkEvent {
    data class Podcast(val id: Long) : DeepLinkEvent
    data object Player : DeepLinkEvent

    /**
     * 공유받은 에피소드를 플레이어에 올린다. 링크가 담고 있던 것을 그대로 옮긴다.
     *
     * `:core:model` 타입을 그대로 실어 나르는 이유는 [DeepLinkEvent] 가 `:app` 소유라
     * `:feature:player` 가 볼 수 없기 때문이다. 여기서 앱 전용 타입으로 갈아입히면 플레이어에
     * 내려보낼 때 같은 필드를 한 번 더 베껴 써야 한다.
     */
    data class Episode(val target: EpisodiveDeepLink.Episode) : DeepLinkEvent
}

sealed interface MainActivityState {
    data object Loading : MainActivityState
    data object FirstLaunch : MainActivityState
    data object NotFirstLaunch : MainActivityState

    fun shouldKeepSplashScreen() = this is Loading
    fun isFirstLaunch() = this is FirstLaunch
}