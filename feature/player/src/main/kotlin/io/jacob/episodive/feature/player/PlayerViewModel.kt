package io.jacob.episodive.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.Progress
import io.jacob.episodive.core.model.Repeat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val content = combine(
        playerRepository.nowPlaying,
        playerRepository.playlist,
        playerRepository.indexOfList,
        playerRepository.progress
    ) { nowPlaying, playlist, indexOfList, progress ->
//        Timber.i("nowPlaying=$nowPlaying, indexOfList=$indexOfList, progress=$progress")
        nowPlaying?.let { np ->
            PlayerContentState(
                nowPlaying = np,
                playlist = playlist,
                indexOfList = indexOfList,
                progress = progress,
            )
        }
    }

    private val meta = combine(
        playerRepository.isPlaying,
        playerRepository.isShuffle,
        playerRepository.repeat,
        playerRepository.speed
    ) { isPlaying, isShuffle, repeat, speed ->
//        Timber.i("isPlaying=$isPlaying, isShuffle=$isShuffle, repeat=$repeat, speed=$speed")
        PlayerMetaState(
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            repeat = repeat,
            speed = speed,
        )
    }

    val state: StateFlow<PlayerState> = combine(
        content, meta
    ) { content, meta ->
        content?.let {
            PlayerState.Ready(
                content = it,
                meta = meta,
            ) as PlayerState
        } ?: PlayerState.Loading
    }
//        .catch { e ->
//        emit(PlayerState.Error(e.message ?: "Unknown Error"))
//    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerState.Loading
        )

    private val _action = MutableSharedFlow<PlayerAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<PlayerEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        handleActions()
//        viewModelScope.launch {
//            state.collectLatest {
//                Timber.i("Player State: $it")
//            }
//        }
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collect { action ->
            when (action) {
                is PlayerAction.PlayOrPause -> playOrPause()
                is PlayerAction.Next -> next()
                is PlayerAction.Previous -> previous()
                is PlayerAction.Shuffle -> shuffle()
                is PlayerAction.Repeat -> repeat()
                is PlayerAction.PlayIndex -> playIndex(action.index)
                is PlayerAction.SeekTo -> seekTo(action.position)
                is PlayerAction.SeekBackward -> seekBackward()
                is PlayerAction.SeekForward -> seekForward()
                is PlayerAction.Speed -> speed(action.speed)
                is PlayerAction.ClickPodcast -> clickPodcast(action.podcast)
                is PlayerAction.ExpandPlayer -> expandPlayer()
                is PlayerAction.CollapsePlayer -> collapsePlayer()
            }
        }
    }

    fun sendAction(action: PlayerAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun playOrPause() = viewModelScope.launch {
        playerRepository.playOrPause()
    }

    private fun next() {
        playerRepository.next()
    }

    private fun previous() {
        playerRepository.previous()
    }

    private fun shuffle() {
        playerRepository.shuffle()
    }

    private fun repeat() {
        playerRepository.changeRepeat()
    }

    private fun playIndex(index: Int) {
        playerRepository.playIndex(index)
    }

    private fun seekTo(position: Long) {
        playerRepository.seekTo(position)
    }

    private fun seekBackward() {
        playerRepository.seekBackward()
    }

    private fun seekForward() {
        playerRepository.seekForward()
    }

    private fun speed(speed: Float) {
        playerRepository.setSpeed(speed)
    }

    private fun clickPodcast(podcast: Podcast) = viewModelScope.launch {
        _effect.emit(PlayerEffect.NavigateToPodcast(podcast))
    }

    private fun expandPlayer() = viewModelScope.launch {
        _effect.emit(PlayerEffect.ShowPlayerBottomSheet)
    }

    private fun collapsePlayer() = viewModelScope.launch {
        _effect.emit(PlayerEffect.HidePlayerBottomSheet)
    }
}

data class PlayerContentState(
    val nowPlaying: Episode,
    val playlist: List<Episode>,
    val indexOfList: Int,
    val progress: Progress,
)

data class PlayerMetaState(
    val isPlaying: Boolean,
    val isShuffle: Boolean,
    val repeat: Repeat,
    val speed: Float,
)

sealed interface PlayerState {
    data object Loading : PlayerState
    data class Ready(
        val content: PlayerContentState,
        val meta: PlayerMetaState,
    ) : PlayerState

    data class Error(val message: String) : PlayerState
}

sealed interface PlayerAction {
    data object PlayOrPause : PlayerAction
    data object Next : PlayerAction
    data object Previous : PlayerAction
    data object Shuffle : PlayerAction
    data object Repeat : PlayerAction
    data class PlayIndex(val index: Int) : PlayerAction
    data class SeekTo(val position: Long) : PlayerAction
    data object SeekBackward : PlayerAction
    data object SeekForward : PlayerAction
    data class Speed(val speed: Float) : PlayerAction
    data class ClickPodcast(val podcast: Podcast) : PlayerAction
    data object ExpandPlayer : PlayerAction
    data object CollapsePlayer : PlayerAction
}

sealed interface PlayerEffect {
    data class NavigateToPodcast(val podcast: Podcast) : PlayerEffect
    data object ShowPlayerBottomSheet : PlayerEffect
    data object HidePlayerBottomSheet : PlayerEffect
}