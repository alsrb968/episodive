package io.jacob.episodive.feature.podcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.episode.GetEpisodesByPodcastIdUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastUseCase
import io.jacob.episodive.core.domain.usecase.podcast.ToggleFollowedUseCase
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PodcastViewModel.Factory::class)
class PodcastViewModel @AssistedInject constructor(
    getPodcastUseCase: GetPodcastUseCase,
    getEpisodesByPodcastIdUseCase: GetEpisodesByPodcastIdUseCase,
    getFollowedPodcastsUseCase: GetFollowedPodcastsUseCase,
    private val toggleFollowedUseCase: ToggleFollowedUseCase,
    @Assisted("id") val id: Long,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("id") id: Long): PodcastViewModel
    }

    val state: StateFlow<PodcastState> = combine(
        getPodcastUseCase(id),
        getEpisodesByPodcastIdUseCase(id),
        getFollowedPodcastsUseCase()
    ) { podcast, episodes, followedPodcasts ->
        if (podcast == null) {
            PodcastState.Error("Podcast not found")
        } else {
            val isFollowed = followedPodcasts.any { it.podcast.id == podcast.id }
            Timber.i("episode size=${episodes.size}, isFollowed=$isFollowed")
            PodcastState.Success(podcast, episodes, isFollowed)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PodcastState.Loading
    )

    private val _action = MutableSharedFlow<PodcastAction>(extraBufferCapacity = 1)

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is PodcastAction.ToggleFollowed -> toggleFollowed()
            }
        }
    }

    fun sendAction(action: PodcastAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun toggleFollowed() = viewModelScope.launch {
        toggleFollowedUseCase(id)
    }
}

sealed interface PodcastState {
    data object Loading : PodcastState
    data class Success(
        val podcast: Podcast,
        val episodes: List<Episode>,
        val isFollowed: Boolean
    ) : PodcastState

    data class Error(val message: String) : PodcastState
}

sealed interface PodcastAction {
    data object ToggleFollowed : PodcastAction
}