package io.jacob.episodive.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.common.combine
import io.jacob.episodive.core.domain.usecase.channel.GetChannelsUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLiveEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetMyRandomEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetPlayingEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.SaveEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.ResumeEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetForeignTrendingPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetLocalTrendingPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserTrendingPodcastsUseCase
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.isRetryable
import io.jacob.episodive.feature.home.navigation.HomeSection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getPlayingEpisodesUseCase: GetPlayingEpisodesUseCase,
    getUserRecentPodcastsUseCase: GetUserRecentPodcastsUseCase,
    getMyRandomEpisodesUseCase: GetMyRandomEpisodesUseCase,
    getUserTrendingPodcastsUseCase: GetUserTrendingPodcastsUseCase,
    getFollowedPodcastsUseCase: GetFollowedPodcastsUseCase,
    getLocalTrendingPodcastsUseCase: GetLocalTrendingPodcastsUseCase,
    getForeignTrendingPodcastsUseCase: GetForeignTrendingPodcastsUseCase,
    getLiveEpisodesUseCase: GetLiveEpisodesUseCase,
    getChannelsUseCase: GetChannelsUseCase,
    private val playEpisodeUseCase: PlayEpisodeUseCase,
    private val resumeEpisodeUseCase: ResumeEpisodeUseCase,
    private val toggleLikedEpisodeUseCase: ToggleLikedEpisodeUseCase,
    private val saveEpisodeUseCase: SaveEpisodeUseCase,
) : ViewModel() {

    // 재시도 시 9개 소스 전부를 다시 구독해야 해서 combine 전체를 flatMapLatest 로 감싼다.
    // 값을 증가시키기만 하면 되므로 트리거 자체의 내용은 의미가 없다.
    private val retryTrigger = MutableStateFlow(0)

    // 각 소스를 SectionState 로 감싸는 것이 핵심이다. 감싸지 않으면 combine 이 아홉 개의 첫
    // 값을 모두 기다려 가장 느린 하나가 홈 전체를 붙잡는다 — 자세한 사정은 SectionState 참고.
    val state: StateFlow<HomeState> = retryTrigger.flatMapLatest {
        combine(
            getPlayingEpisodesUseCase(max = FEED_MAX).asSectionState(),
            getUserRecentPodcastsUseCase(max = FEED_MAX).asSectionState(),
            getMyRandomEpisodesUseCase(max = COMPACT_MAX).asSectionState(),
            getUserTrendingPodcastsUseCase(max = FEED_MAX).asSectionState(),
            getFollowedPodcastsUseCase(max = FEED_MAX).asSectionState(),
            getLocalTrendingPodcastsUseCase(max = FEED_MAX).asSectionState(),
            getForeignTrendingPodcastsUseCase(max = FEED_MAX).asSectionState(),
            getLiveEpisodesUseCase(max = COMPACT_MAX).asSectionState(),
            getChannelsUseCase().asSectionState(),
        ) {
                playingEpisodes,
                userRecentPodcasts,
                randomEpisodes,
                userTrendingPodcasts,
                followedPodcasts,
                localTrendingPodcasts,
                foreignTrendingPodcasts,
                liveEpisodes,
                channels,
            ->

            // 섹션 목록을 여기서 다시 세지 않고 상태 자신에게 묻는다 — 두 벌로 갈라지면
            // 새 섹션을 더할 때 한쪽만 고쳐 판정이 조용히 어긋난다.
            val loaded = HomeState.Success(
                playingEpisodes = playingEpisodes,
                userRecentPodcasts = userRecentPodcasts,
                randomEpisodes = randomEpisodes,
                userTrendingPodcasts = userTrendingPodcasts,
                followedPodcasts = followedPodcasts,
                localTrendingPodcasts = localTrendingPodcasts,
                foreignTrendingPodcasts = foreignTrendingPodcasts,
                liveEpisodes = liveEpisodes,
                channels = channels,
            )

            when {
                // 화면 전체를 덮는 두 상태는 "모두 그렇다" 일 때만 쓴다. 하나라도 보여줄 것이
                // 있으면 그것을 띄우고 나머지는 각자 자기 자리에서 기다리거나 빠진다.
                loaded.sections.all { it is SectionState.Loading } -> HomeState.Loading

                loaded.sections.all { it is SectionState.Error } -> {
                    val errors = loaded.sections.filterIsInstance<SectionState.Error>()
                    // 재시도할 수 있는 오류를 먼저 고른다. 그냥 첫 번째를 쓰면 재시도 불가
                    // 오류(Unauthorized·NotFound)가 섞였을 때 사용자가 재시도 버튼조차 받지
                    // 못하고 갇힌다.
                    HomeState.Error(
                        errors.firstOrNull { it.error.isRetryable }?.error ?: errors.first().error
                    )
                }

                else -> loaded
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState.Loading
    )

    private val _action = MutableSharedFlow<HomeAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is HomeAction.PlayEpisode -> playEpisode(action.episode)
                is HomeAction.ResumeEpisode -> resumeEpisode(action.playedEpisode)
                is HomeAction.ToggleLikedEpisode -> toggleLikedEpisode(action.episode)
                is HomeAction.ToggleSavedEpisode -> toggleSavedEpisode(action.episode)
                is HomeAction.ClickPodcast -> clickPodcast(action.podcastId)
                is HomeAction.ClickChannel -> clickChannel(action.channelId)
                is HomeAction.ClickMore -> clickMore(action.section)
                is HomeAction.Retry -> retry()
            }
        }
    }

    fun sendAction(action: HomeAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun playEpisode(episode: Episode) = viewModelScope.launch {
        playEpisodeUseCase(episode)
    }

    private fun resumeEpisode(playedEpisode: Episode) = viewModelScope.launch {
        resumeEpisodeUseCase(playedEpisode)
    }

    private fun toggleLikedEpisode(episode: Episode) = viewModelScope.launch {
        toggleLikedEpisodeUseCase(episode)
    }

    private fun toggleSavedEpisode(episode: Episode) = viewModelScope.launch {
        val isSavedNow = saveEpisodeUseCase(episode)
        if (!isSavedNow) {
            _effect.emit(HomeEffect.ShowUnsaveSnackbar(episode))
        }
    }

    private fun clickPodcast(podcastId: Long) = viewModelScope.launch {
        _effect.emit(HomeEffect.NavigateToPodcast(podcastId))
    }

    private fun clickChannel(channelId: Long) = viewModelScope.launch {
        _effect.emit(HomeEffect.NavigateToChannel(channelId))
    }

    private fun clickMore(section: HomeSection) = viewModelScope.launch {
        _effect.emit(HomeEffect.NavigateToMore(section))
    }

    private fun retry() {
        retryTrigger.update { it + 1 }
    }

    companion object {
        private const val FEED_MAX = 10
        private const val COMPACT_MAX = 6
    }
}

sealed interface HomeState {
    /** 아직 아무 섹션도 응답하지 않았다. 이 상태에서만 화면 전체가 스켈레톤이다. */
    data object Loading : HomeState

    /**
     * 섹션 중 적어도 하나가 판정됐다. **나머지가 아직 오지 않았어도 이 상태다** — 각 섹션이
     * 자기 [SectionState] 를 들고 있어, 화면은 도착한 것을 그리고 나머지 자리에는 스켈레톤을 둔다.
     */
    data class Success(
        val playingEpisodes: SectionState<Episode>,
        val userRecentPodcasts: SectionState<Podcast>,
        val randomEpisodes: SectionState<Episode>,
        val userTrendingPodcasts: SectionState<Podcast>,
        val followedPodcasts: SectionState<Podcast>,
        val localTrendingPodcasts: SectionState<Podcast>,
        val foreignTrendingPodcasts: SectionState<Podcast>,
        val liveEpisodes: SectionState<Episode>,
        val channels: SectionState<Channel>,
    ) : HomeState {
        /**
         * 섹션 전체를 한 번에 훑어야 할 때. 화면을 통째로 덮을지(모두 로딩·모두 실패)를 여기서
         * 가른다. **섹션을 더하면 이 목록에도 더한다** — 빠뜨리면 그 섹션만 판정에서 조용히
         * 빠져, 예컨대 그것 하나만 남아 로딩 중인데도 화면이 다 온 척한다.
         */
        val sections: List<SectionState<*>>
            get() = listOf(
                playingEpisodes,
                userRecentPodcasts,
                randomEpisodes,
                userTrendingPodcasts,
                followedPodcasts,
                localTrendingPodcasts,
                foreignTrendingPodcasts,
                liveEpisodes,
                channels,
            )
    }

    /** 모든 섹션이 실패했다. 하나라도 살아 있으면 [Success] 안의 섹션 오류로 다룬다. */
    data class Error(val error: DataError) : HomeState
}

sealed interface HomeAction {
    data class PlayEpisode(val episode: Episode) : HomeAction
    data class ResumeEpisode(val playedEpisode: Episode) : HomeAction
    data class ToggleLikedEpisode(val episode: Episode) : HomeAction
    data class ToggleSavedEpisode(val episode: Episode) : HomeAction
    data class ClickPodcast(val podcastId: Long) : HomeAction
    data class ClickChannel(val channelId: Long) : HomeAction
    data class ClickMore(val section: HomeSection) : HomeAction
    data object Retry : HomeAction
}

sealed interface HomeEffect {
    data class NavigateToPodcast(val podcastId: Long) : HomeEffect
    data class NavigateToChannel(val channelId: Long) : HomeEffect
    data class NavigateToMore(val section: HomeSection) : HomeEffect
    data class ShowUnsaveSnackbar(val episode: Episode) : HomeEffect
}