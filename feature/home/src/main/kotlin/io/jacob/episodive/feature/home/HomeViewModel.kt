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
import io.jacob.episodive.core.domain.usecase.player.GetPlayingEpisodeIdUseCase
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
import io.jacob.episodive.core.model.asDataError
import io.jacob.episodive.feature.home.navigation.HomeSection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
    getPlayingEpisodeIdUseCase: GetPlayingEpisodeIdUseCase,
    private val playEpisodeUseCase: PlayEpisodeUseCase,
    private val resumeEpisodeUseCase: ResumeEpisodeUseCase,
    private val toggleLikedEpisodeUseCase: ToggleLikedEpisodeUseCase,
    private val saveEpisodeUseCase: SaveEpisodeUseCase,
) : ViewModel() {

    // 재시도 시 9개 소스 전부를 다시 구독해야 해서 combine 전체를 flatMapLatest 로 감싼다.
    // 값을 증가시키기만 하면 되므로 트리거 자체의 내용은 의미가 없다.
    private val retryTrigger = MutableStateFlow(0)

    val state: StateFlow<HomeState> = retryTrigger.flatMapLatest {
        combine(
            getPlayingEpisodesUseCase(max = FEED_MAX),
            getUserRecentPodcastsUseCase(max = FEED_MAX),
            getMyRandomEpisodesUseCase(max = COMPACT_MAX),
            getUserTrendingPodcastsUseCase(max = FEED_MAX),
            getFollowedPodcastsUseCase(max = FEED_MAX),
            getLocalTrendingPodcastsUseCase(max = FEED_MAX),
            getForeignTrendingPodcastsUseCase(max = FEED_MAX),
            getLiveEpisodesUseCase(max = COMPACT_MAX),
            getChannelsUseCase(),
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

            HomeState.Success(
                playingEpisodes = playingEpisodes,
                userRecentPodcasts = userRecentPodcasts,
                randomEpisodes = randomEpisodes,
                userTrendingPodcasts = userTrendingPodcasts,
                followedPodcasts = followedPodcasts,
                localTrendingPodcasts = localTrendingPodcasts,
                foreignTrendingPodcasts = foreignTrendingPodcasts,
                liveEpisodes = liveEpisodes,
                channels = channels
            ) as HomeState
        }.catch { e ->
            Timber.e(e, "홈 데이터를 불러오지 못했다")
            emit(HomeState.Error(e.asDataError()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState.Loading
    )

    /**
     * 이어듣기 카드에 "재생 중" 을 띄우기 위한 값. 위 [state] 의 combine 에 넣지 않는다 —
     * 재생/일시정지를 누를 때마다 홈 데이터 아홉 갈래가 통째로 재계산될 이유가 없다.
     */
    val playingEpisodeId: StateFlow<Long?> = getPlayingEpisodeIdUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
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
    data object Loading : HomeState
    data class Success(
        val playingEpisodes: List<Episode>,
        val userRecentPodcasts: List<Podcast>,
        val randomEpisodes: List<Episode>,
        val userTrendingPodcasts: List<Podcast>,
        val followedPodcasts: List<Podcast>,
        val localTrendingPodcasts: List<Podcast>,
        val foreignTrendingPodcasts: List<Podcast>,
        val liveEpisodes: List<Episode>,
        val channels: List<Channel>,
    ) : HomeState

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