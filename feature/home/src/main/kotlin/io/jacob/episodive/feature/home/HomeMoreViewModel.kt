package io.jacob.episodive.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.channel.GetChannelsUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLiveEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetMyRandomEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.SaveEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetForeignTrendingPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetLocalTrendingPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserTrendingPodcastsPagingUseCase
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.asDataError
import io.jacob.episodive.feature.home.navigation.HomeSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = HomeMoreViewModel.Factory::class)
class HomeMoreViewModel @AssistedInject constructor(
    getUserRecentPodcastsPagingUseCase: GetUserRecentPodcastsPagingUseCase,
    getMyRandomEpisodesPagingUseCase: GetMyRandomEpisodesPagingUseCase,
    getUserTrendingPodcastsPagingUseCase: GetUserTrendingPodcastsPagingUseCase,
    getFollowedPodcastsPagingUseCase: GetFollowedPodcastsPagingUseCase,
    getLocalTrendingPodcastsPagingUseCase: GetLocalTrendingPodcastsPagingUseCase,
    getForeignTrendingPodcastsPagingUseCase: GetForeignTrendingPodcastsPagingUseCase,
    getLiveEpisodesPagingUseCase: GetLiveEpisodesPagingUseCase,
    getChannelsUseCase: GetChannelsUseCase,
    private val playEpisodeUseCase: PlayEpisodeUseCase,
    private val toggleLikedEpisodeUseCase: ToggleLikedEpisodeUseCase,
    private val saveEpisodeUseCase: SaveEpisodeUseCase,
    @Assisted("section") val section: HomeSection,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("section") section: HomeSection): HomeMoreViewModel
    }

    // 채널 섹션에만 쓰인다. 나머지는 Paging 이 자체 재시도를 갖고 있다.
    private val retryTrigger = MutableStateFlow(0)

    /**
     * 이 화면이 보여줄 것. 섹션은 화면 수명 동안 바뀌지 않으므로 한 번만 정한다.
     *
     * Paging 스트림은 상태 체인 밖에 둔다 — Paging 은 자체 로드 상태와 재시도 경로를 갖고
     * 있어서, 상태 흐름 안에 넣으면 두 개의 로딩 개념이 겹친다.
     */
    val content: HomeMoreContent = when (section) {
        HomeSection.MyRecentPodcasts ->
            HomeMoreContent.PodcastPaging(
                getUserRecentPodcastsPagingUseCase(max = PODCAST_MORE_MAX).cachedIn(viewModelScope)
            )

        HomeSection.RandomEpisodes ->
            HomeMoreContent.EpisodePaging(
                getMyRandomEpisodesPagingUseCase(max = EPISODE_MORE_MAX).cachedIn(viewModelScope)
            )

        HomeSection.MyTrendingPodcasts ->
            HomeMoreContent.PodcastPaging(
                getUserTrendingPodcastsPagingUseCase(max = PODCAST_MORE_MAX).cachedIn(viewModelScope)
            )

        HomeSection.FollowedPodcasts ->
            // 구독 목록은 로컬 DB 라 원격 상한이 없다.
            HomeMoreContent.PodcastPaging(
                getFollowedPodcastsPagingUseCase().cachedIn(viewModelScope)
            )

        HomeSection.LocalTrendingPodcasts ->
            HomeMoreContent.PodcastPaging(
                getLocalTrendingPodcastsPagingUseCase(max = PODCAST_MORE_MAX).cachedIn(viewModelScope)
            )

        HomeSection.ForeignTrendingPodcasts ->
            HomeMoreContent.PodcastPaging(
                getForeignTrendingPodcastsPagingUseCase(max = PODCAST_MORE_MAX)
                    .cachedIn(viewModelScope)
            )

        HomeSection.LiveEpisodes ->
            HomeMoreContent.EpisodePaging(
                getLiveEpisodesPagingUseCase(max = EPISODE_MORE_MAX).cachedIn(viewModelScope)
            )

        HomeSection.Channels ->
            HomeMoreContent.ChannelList(
                retryTrigger.flatMapLatest {
                    getChannelsUseCase().map { HomeMoreChannelState.Success(it) as HomeMoreChannelState }
                        .catch { e ->
                            Timber.e(e, "채널 목록을 불러오지 못했다")
                            emit(HomeMoreChannelState.Error(e.asDataError()))
                        }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = HomeMoreChannelState.Loading,
                )
            )
    }

    private val _action = MutableSharedFlow<HomeMoreAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<HomeMoreEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is HomeMoreAction.PlayEpisode -> playEpisode(action.episode)
                is HomeMoreAction.ToggleLikedEpisode -> toggleLikedEpisode(action.episode)
                is HomeMoreAction.ToggleSavedEpisode -> toggleSavedEpisode(action.episode)
                is HomeMoreAction.ClickPodcast -> clickPodcast(action.podcastId)
                is HomeMoreAction.ClickChannel -> clickChannel(action.channelId)
                is HomeMoreAction.ClickBack -> clickBack()
                is HomeMoreAction.Retry -> retry()
            }
        }
    }

    fun sendAction(action: HomeMoreAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun playEpisode(episode: Episode) = viewModelScope.launch {
        playEpisodeUseCase(episode)
    }

    private fun toggleLikedEpisode(episode: Episode) = viewModelScope.launch {
        toggleLikedEpisodeUseCase(episode)
    }

    private fun toggleSavedEpisode(episode: Episode) = viewModelScope.launch {
        val isSavedNow = saveEpisodeUseCase(episode)
        if (!isSavedNow) {
            _effect.emit(HomeMoreEffect.ShowUnsaveSnackbar(episode))
        }
    }

    private fun clickPodcast(podcastId: Long) = viewModelScope.launch {
        _effect.emit(HomeMoreEffect.NavigateToPodcast(podcastId))
    }

    private fun clickChannel(channelId: Long) = viewModelScope.launch {
        _effect.emit(HomeMoreEffect.NavigateToChannel(channelId))
    }

    private fun clickBack() = viewModelScope.launch {
        _effect.emit(HomeMoreEffect.NavigateBack)
    }

    private fun retry() {
        retryTrigger.update { it + 1 }
    }

    companion object {
        /**
         * 팟캐스트 목록의 상한.
         *
         * 에피소드보다 작은 이유는 원격 비용이 다르기 때문이다. 트렌딩·최근 경로는 피드
         * 목록을 받은 뒤 **피드마다 상세 요청을 한 번씩 더** 보내므로 개수가 곧 요청 수가
         * 된다. 반면 에피소드 경로는 한 번의 요청으로 끝난다.
         */
        private const val PODCAST_MORE_MAX = 50
        private const val EPISODE_MORE_MAX = 100
    }
}

/**
 * 섹션별로 무엇을 어떤 방식으로 흘려보낼지.
 *
 * 채널만 [ChannelList] 인 것은 채널 저장소에 로컬 캐시도 PagingSource 도 없어서다 — 이미
 * 한 번의 원격 응답으로 전체가 온다. 여기에 Paging 을 씌우면 껍데기만 늘고, 원격 실패가
 * 상류 Flow 예외로 터져 화면이 그대로 죽는다.
 */
sealed interface HomeMoreContent {
    data class PodcastPaging(val items: Flow<PagingData<Podcast>>) : HomeMoreContent
    data class EpisodePaging(val items: Flow<PagingData<Episode>>) : HomeMoreContent
    data class ChannelList(val state: StateFlow<HomeMoreChannelState>) : HomeMoreContent
}

sealed interface HomeMoreChannelState {
    data object Loading : HomeMoreChannelState
    data class Success(val channels: List<Channel>) : HomeMoreChannelState
    data class Error(val error: DataError) : HomeMoreChannelState
}

sealed interface HomeMoreAction {
    data class PlayEpisode(val episode: Episode) : HomeMoreAction
    data class ToggleLikedEpisode(val episode: Episode) : HomeMoreAction
    data class ToggleSavedEpisode(val episode: Episode) : HomeMoreAction
    data class ClickPodcast(val podcastId: Long) : HomeMoreAction
    data class ClickChannel(val channelId: Long) : HomeMoreAction
    data object ClickBack : HomeMoreAction
    data object Retry : HomeMoreAction
}

sealed interface HomeMoreEffect {
    data object NavigateBack : HomeMoreEffect
    data class NavigateToPodcast(val podcastId: Long) : HomeMoreEffect
    data class NavigateToChannel(val channelId: Long) : HomeMoreEffect
    data class ShowUnsaveSnackbar(val episode: Episode) : HomeMoreEffect
}
