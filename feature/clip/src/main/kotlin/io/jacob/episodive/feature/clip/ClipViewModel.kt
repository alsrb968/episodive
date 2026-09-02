package io.jacob.episodive.feature.clip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.domain.usecase.episode.GetClipEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Playback
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.model.Spectrum
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ClipViewModel @Inject constructor(
    getClipEpisodesPagingUseCase: GetClipEpisodesPagingUseCase,
    @param:Player(EpisodivePlayers.Clip) private val playerRepository: PlayerRepository,
    private val playEpisodeUseCase: PlayEpisodeUseCase,
    private val toggleLikedEpisodeUseCase: ToggleLikedEpisodeUseCase,
) : ViewModel() {
    val episodes = getClipEpisodesPagingUseCase(100).cachedIn(viewModelScope)

    val clipPlayerState: StateFlow<ClipPlayerState> = combine(
        playerRepository.playback,
        playerRepository.progress,
        playerRepository.isPlaying,
    ) { playback, progress, isPlaying ->
        ClipPlayerState(
            playback = playback,
            progress = progress,
            isPlaying = isPlaying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClipPlayerState()
    )

    /**
     * 지금 나고 있는 소리를 주파수 대역 다섯 칸으로 나눈 세기(각 0..1).
     *
     * clipPlayerState 에 섞지 않는다. 초당 서른 번 바뀌는 값을 화면 상태에 넣으면 그 빈도로
     * 화면 전체가 재구성된다. 파형 막대 다섯만 쓰는 값이므로 통로도 따로 둔다.
     */
    val spectrum: StateFlow<Spectrum> = playerRepository.spectrum.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Spectrum.Silent,
    )

    private val _action = MutableSharedFlow<ClipAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<ClipEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    /**
     * 사용자가 직접 일시정지했는지. 화면이 다시 보일 때([ClipAction.Resume]) 무조건 재생하면
     * 사용자가 세워 둔 것을 앱이 되돌려 버린다. 새 클립을 재생하는 순간 다시 내린다.
     */
    private var pausedByUser = false

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is ClipAction.PlayClip -> playClip(action.episode)
                is ClipAction.TogglePlay -> togglePlay(action.episode, action.play)
                is ClipAction.ClickEpisode -> playEpisode(action.episode)
                is ClipAction.ToggleLikedEpisode -> toggleLikedEpisode(action.episode)
                is ClipAction.ClickPodcast -> clickPodcast(action.podcastId)
                is ClipAction.Resume -> resume()
                is ClipAction.Pause -> pause()
            }
        }
    }

    fun sendAction(action: ClipAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun playClip(episode: Episode) {
        pausedByUser = false
        playerRepository.playClip(episode)
    }

    /**
     * 재생 버튼 토글. [play] 는 사용자가 원하는 방향이다.
     *
     * 재생 쪽은 [PlayerRepository.playClip] 하나로 보낸다. "이미 올라 있는 그 클립인가" 는
     * 플레이어에 실제로 무엇이 올라 있는지를 아는 [PlayerRepository] 쪽이 판정해 그 자리에서
     * 이어 튼다. 여기서 같은 판정을 한 번 더 하면 두 곳이 갈라진다 — 특히 같은 에피소드라도
     * 잘라낸 창이 바뀐 경우를 여기서는 알 수 없어, 옛 창을 이어 틀면서 카드는 새 길이를 띄운다.
     */
    private fun togglePlay(episode: Episode, play: Boolean) {
        if (play) {
            playClip(episode)
        } else {
            pausedByUser = true
            playerRepository.pause()
        }
    }

    /**
     * 화면이 다시 보일 때의 자동 재개. 사용자가 직접 세워 둔 것은 그대로 둔다.
     */
    private fun resume() {
        if (pausedByUser) return
        playerRepository.resume()
    }

    /** 화면이 가려질 때의 자동 정지. 사용자의 의도가 아니므로 [pausedByUser] 를 올리지 않는다. */
    private fun pause() {
        playerRepository.pause()
    }

    private fun playEpisode(episode: Episode) = viewModelScope.launch {
        playEpisodeUseCase(episode)
    }

    private fun toggleLikedEpisode(episode: Episode) = viewModelScope.launch {
        toggleLikedEpisodeUseCase(episode)
    }


    private fun clickPodcast(podcastId: Long) = viewModelScope.launch {
        _effect.emit(ClipEffect.NavigateToPodcast(podcastId))
    }
}

data class ClipPlayerState(
    val playback: Playback = Playback.IDLE,
    val progress: Progress = Progress(0.seconds, 0.seconds, 0.seconds),
    val isPlaying: Boolean = false,
)

sealed interface ClipAction {
    data class PlayClip(val episode: Episode) : ClipAction

    /** 재생 버튼 토글. [play] 가 `true` 면 재생, `false` 면 일시정지. */
    data class TogglePlay(val episode: Episode, val play: Boolean) : ClipAction

    data class ClickEpisode(val episode: Episode) : ClipAction
    data class ToggleLikedEpisode(val episode: Episode) : ClipAction
    data class ClickPodcast(val podcastId: Long) : ClipAction
    data object Resume : ClipAction
    data object Pause : ClipAction
}

sealed interface ClipEffect {
    data class NavigateToPodcast(val podcastId: Long) : ClipEffect
}