package io.jacob.episodive.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.channel.GetChannelByIdUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastsByChannelUseCase
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.asDataError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = ChannelViewModel.Factory::class)
class ChannelViewModel @AssistedInject constructor(
    getChannelByIdUseCase: GetChannelByIdUseCase,
    getPodcastsByChannelUseCase: GetPodcastsByChannelUseCase,
    @Assisted("id") val id: Long,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("id") id: Long): ChannelViewModel
    }

    // 재시도는 소스 체인을 통째로 재구독해야 한다. 아래 체인은 전부 콜드 Flow라
    // 재구독해도 부작용이 없다는 걸 확인했다.
    private val retryTrigger = MutableStateFlow(0)

    val state: StateFlow<ChannelState> = retryTrigger.flatMapLatest {
        val channel = getChannelByIdUseCase(id)
        // channel 이 null 일 때 emptyFlow() 를 주면 combine 이 값을 한 번도 내보내지 않아
        // 아래 not-found 분기가 영원히 도달하지 않는다(Loading 에 영구히 멈춤). flowOf 로
        // 빈 리스트를 한 번 방출해야 combine 이 실제로 값을 낸다.
        val podcasts = channel.flatMapLatest { c ->
            if (c == null) flowOf(emptyList()) else getPodcastsByChannelUseCase(c)
        }

        combine(channel, podcasts) { c, p ->
            if (c == null) {
                ChannelState.Error(DataError.NotFound)
            } else {
                ChannelState.Success(
                    channel = c,
                    podcasts = p,
                )
            }
        }.catch { e ->
            Timber.e(e, "채널을 불러오지 못했다 (id=$id)")
            emit(ChannelState.Error(e.asDataError()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChannelState.Loading
    )

    private val _action = MutableSharedFlow<ChannelAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<ChannelEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is ChannelAction.ClickBack -> clickBack()
                is ChannelAction.ClickPodcast -> clickPodcast(action.podcastId)
                is ChannelAction.Retry -> retry()
            }
        }
    }

    fun sendAction(action: ChannelAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun clickBack() = viewModelScope.launch {
        _effect.emit(ChannelEffect.NavigateBack)
    }

    private fun clickPodcast(podcastId: Long) = viewModelScope.launch {
        _effect.emit(ChannelEffect.NavigateToPodcast(podcastId))
    }

    private fun retry() {
        retryTrigger.update { it + 1 }
    }
}

sealed interface ChannelState {
    data object Loading : ChannelState
    data class Success(
        val channel: Channel,
        val podcasts: List<Podcast>,
    ) : ChannelState

    data class Error(val error: DataError) : ChannelState
}

sealed interface ChannelAction {
    data object ClickBack : ChannelAction
    data class ClickPodcast(val podcastId: Long) : ChannelAction
    data object Retry : ChannelAction
}

sealed interface ChannelEffect {
    data object NavigateBack : ChannelEffect
    data class NavigateToPodcast(val podcastId: Long) : ChannelEffect
}