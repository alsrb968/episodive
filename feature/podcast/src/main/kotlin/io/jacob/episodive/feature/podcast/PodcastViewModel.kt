package io.jacob.episodive.feature.podcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.episode.GetEpisodesByPodcastIdUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastUseCase
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = PodcastViewModel.Factory::class)
class PodcastViewModel @AssistedInject constructor(
    getPodcastUseCase: GetPodcastUseCase,
    getEpisodesByPodcastIdUseCase: GetEpisodesByPodcastIdUseCase,
    @Assisted("id") val id: Long,
) : ViewModel() {
    val state: StateFlow<PodcastState> = combine(
        getPodcastUseCase(id),
        getEpisodesByPodcastIdUseCase(id)
    ) { podcast, episodes ->
        if (podcast == null) {
            PodcastState.Error("Podcast not found")
        } else {
            PodcastState.Success(podcast, episodes)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PodcastState.Loading
    )

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("id") id: Long): PodcastViewModel
    }
}

sealed interface PodcastState {
    data object Loading : PodcastState
    data class Success(val podcast: Podcast, val episodes: List<Episode>) : PodcastState
    data class Error(val message: String) : PodcastState
}