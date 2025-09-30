package io.jacob.episodive.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.episode.GetLiveEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetMyRandomEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetPlayingEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.feed.GetMyRecentFeedsUseCase
import io.jacob.episodive.core.domain.usecase.feed.GetMyTrendingFeedsUseCase
import io.jacob.episodive.core.domain.usecase.feed.GetTrendingFeedsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.user.GetUserDataUseCase
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.FollowedPodcast
import io.jacob.episodive.core.model.PlayedEpisode
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.TrendingFeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getPlayingEpisodesUseCase: GetPlayingEpisodesUseCase,
    private val getMyRecentFeedsUseCase: GetMyRecentFeedsUseCase,
    private val getMyRandomEpisodesUseCase: GetMyRandomEpisodesUseCase,
    private val getMyTrendingFeedsUseCase: GetMyTrendingFeedsUseCase,
    private val getFollowedPodcastsUseCase: GetFollowedPodcastsUseCase,
    private val getTrendingFeedsUseCase: GetTrendingFeedsUseCase,
    private val getLiveEpisodesUseCase: GetLiveEpisodesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state = _state.asStateFlow()

    init {
        loadContents()
    }

    private fun loadContents() = viewModelScope.launch {
        try {
            val userData = getUserDataUseCase().first()
            val languages = listOf("en", "es", "fr", "de", "it", "ja", "ko", "pt", "ru", "zh")
            val foreignLanguages = languages.filter { it != userData.language }
                .joinToString(",") { it }

            val playingEpisodes = getPlayingEpisodesUseCase().first().take(10)
            val myRecentFeeds = getMyRecentFeedsUseCase().first().take(10)
            val randomEpisodes = emptyList<Episode>() //getMyRandomEpisodesUseCase().first().take(6)
            val myTrendingFeeds = getMyTrendingFeedsUseCase().first().take(10)
            val followedPodcasts = getFollowedPodcastsUseCase().first().take(10)
            val localTrendingFeeds =
                getTrendingFeedsUseCase(language = userData.language).first().take(10)
            val foreignTrendingFeeds =
                getTrendingFeedsUseCase(language = foreignLanguages).first().take(10)
            val liveEpisodes = getLiveEpisodesUseCase().first().take(6)

            _state.emit(
                HomeState.Success(
                    playingEpisodes = playingEpisodes,
                    myRecentFeeds = myRecentFeeds,
                    randomEpisodes = randomEpisodes,
                    myTrendingFeeds = myTrendingFeeds,
                    followedPodcasts = followedPodcasts,
                    localTrendingFeeds = localTrendingFeeds,
                    foreignTrendingFeeds = foreignTrendingFeeds,
                    liveEpisodes = liveEpisodes,
                )
            )
        } catch (e: Exception) {
            _state.value = HomeState.Error(e.message ?: "An unknown error occurred")
        }
    }
}

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(
        val playingEpisodes: List<PlayedEpisode>,
        val myRecentFeeds: List<RecentFeed>,
        val randomEpisodes: List<Episode>,
        val myTrendingFeeds: List<TrendingFeed>,
        val followedPodcasts: List<FollowedPodcast>,
        val localTrendingFeeds: List<TrendingFeed>,
        val foreignTrendingFeeds: List<TrendingFeed>,
        val liveEpisodes: List<Episode>,
    ) : HomeState

    data class Error(val message: String) : HomeState
}