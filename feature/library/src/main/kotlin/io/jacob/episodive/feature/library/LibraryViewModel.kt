package io.jacob.episodive.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.common.combine
import io.jacob.episodive.core.domain.usecase.FindInLibraryUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetAllPlayedEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetAllPlayedEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLikedEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetLikedEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetSavedEpisodesUseCase
import io.jacob.episodive.core.domain.usecase.episode.GetSavedEpisodesPagingUseCase
import io.jacob.episodive.core.domain.usecase.episode.SaveEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.episode.ToggleLikedEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.opml.ExportOpmlUseCase
import io.jacob.episodive.core.domain.usecase.opml.ImportOpmlUseCase
import io.jacob.episodive.core.domain.usecase.player.PlayEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.player.ResumeEpisodeUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsOnceUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.ToggleFollowedUseCase
import io.jacob.episodive.core.domain.usecase.user.GetPreferredCategoriesUseCase
import io.jacob.episodive.core.domain.usecase.user.GetSelectableCategoriesUseCase
import io.jacob.episodive.core.domain.usecase.user.ToggleCategoryUseCase
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.LibraryFindResult
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.model.asDataError
import io.jacob.episodive.core.model.mapper.toHumanReadable
import io.jacob.episodive.core.model.opml.OpmlImportProgress
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val findInLibraryUseCase: FindInLibraryUseCase,
    getAllPlayedEpisodesUseCase: GetAllPlayedEpisodesUseCase,
    getLikedEpisodesUseCase: GetLikedEpisodesUseCase,
    getSavedEpisodesUseCase: GetSavedEpisodesUseCase,
    getSavedEpisodesPagingUseCase: GetSavedEpisodesPagingUseCase,
    getFollowedPodcastsUseCase: GetFollowedPodcastsUseCase,
    getPreferredCategoriesUseCase: GetPreferredCategoriesUseCase,
    getSelectableCategoriesUseCase: GetSelectableCategoriesUseCase,
    getAllPlayedEpisodesPagingUseCase: GetAllPlayedEpisodesPagingUseCase,
    getLikedEpisodesPagingUseCase: GetLikedEpisodesPagingUseCase,
    getFollowedPodcastsPagingUseCase: GetFollowedPodcastsPagingUseCase,
    private val playEpisodeUseCase: PlayEpisodeUseCase,
    private val resumeEpisodeUseCase: ResumeEpisodeUseCase,
    private val toggleLikedEpisodeUseCase: ToggleLikedEpisodeUseCase,
    private val saveEpisodeUseCase: SaveEpisodeUseCase,
    private val toggleFollowedUseCase: ToggleFollowedUseCase,
    private val toggleCategoryUseCase: ToggleCategoryUseCase,
    private val getFollowedPodcastsOnceUseCase: GetFollowedPodcastsOnceUseCase,
    private val exportOpmlUseCase: ExportOpmlUseCase,
    private val importOpmlUseCase: ImportOpmlUseCase,
) : ViewModel() {
    private val _findQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val _findResult: Flow<LibraryFindResult> = _findQuery
        .debounce(500L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isNotEmpty()) {
                findInLibraryUseCase(query)
            } else {
                flowOf(LibraryFindResult())
            }
        }

    private val _section = MutableStateFlow(LibrarySection.All)

    // 재시도는 state 를 구성하는 소스 체인을 통째로 재구독해야 한다. 아래 소스들은 전부
    // 재구독해도 부작용이 없는 것을 확인했다 (Paging 소스 3개는 이 체인과 분리돼 있어 대상이 아님).
    private val retryTrigger = MutableStateFlow(0)

    val playedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>> =
        getAllPlayedEpisodesPagingUseCase().map { pagingData ->
            pagingData
                .map { episode -> SeparatedUiModel.Content(episode) }
                .insertSeparators { before, after ->
                    if (after == null) {
                        return@insertSeparators null
                    }

                    val beforeDate = before?.data?.playedAt?.toHumanReadable()
                    val afterDate = after.data.playedAt?.toHumanReadable()

                    if (before == null || beforeDate != afterDate) {
                        afterDate?.let { SeparatedUiModel.Separator(it) }
                    } else {
                        null
                    }
                }
        }.cachedIn(viewModelScope)

    val likedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>> =
        getLikedEpisodesPagingUseCase().map { pagingData ->
            pagingData
                .map { episode -> SeparatedUiModel.Content(episode) }
                .insertSeparators { before, after ->
                    if (after == null) {
                        return@insertSeparators null
                    }

                    val beforeDate = before?.data?.likedAt?.toHumanReadable()
                    val afterDate = after.data.likedAt?.toHumanReadable()

                    if (before == null || beforeDate != afterDate) {
                        afterDate?.let { SeparatedUiModel.Separator(it) }
                    } else {
                        null
                    }
                }
        }.cachedIn(viewModelScope)

    val savedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>> =
        getSavedEpisodesPagingUseCase().map { pagingData ->
            pagingData
                .map { episode -> SeparatedUiModel.Content(episode) }
                .insertSeparators { before, after ->
                    if (after == null) {
                        return@insertSeparators null
                    }

                    val beforeDate = before?.data?.savedAt?.toHumanReadable()
                    val afterDate = after.data.savedAt?.toHumanReadable()

                    if (before == null || beforeDate != afterDate) {
                        afterDate?.let { SeparatedUiModel.Separator(it) }
                    } else {
                        null
                    }
                }
        }.cachedIn(viewModelScope)

    val followedPodcastsPaging: Flow<PagingData<SeparatedUiModel<Podcast>>> =
        getFollowedPodcastsPagingUseCase().map { pagingData ->
            pagingData
                .map { podcast -> SeparatedUiModel.Content(podcast) }
                .insertSeparators { before, after ->
                    if (after == null) {
                        return@insertSeparators null
                    }

                    val beforeDate = before?.data?.followedAt?.toHumanReadable()
                    val afterDate = after.data.followedAt?.toHumanReadable()

                    if (before == null || beforeDate != afterDate) {
                        afterDate?.let { SeparatedUiModel.Separator(it) }
                    } else {
                        null
                    }
                }
        }.cachedIn(viewModelScope)

    val state: StateFlow<LibraryState> = retryTrigger.flatMapLatest {
        combine(
            _findQuery,
            _findResult,
            getAllPlayedEpisodesUseCase(max = SECTION_MAX),
            getLikedEpisodesUseCase(max = SECTION_MAX),
            getSavedEpisodesUseCase(max = SECTION_MAX),
            getFollowedPodcastsUseCase(max = SECTION_MAX),
            getPreferredCategoriesUseCase(),
            getSelectableCategoriesUseCase(),
            _section
        ) { query, result, allPlayedEpisodes, likedEpisodes, savedEpisodes, followedPodcasts, preferredCategories, selectableCategories, section ->
            if (query.isEmpty() && result.isAllEmpty) {
                LibraryState.Success(
                    findQuery = query,
                    allPlayedEpisodes = allPlayedEpisodes,
                    likedEpisodes = likedEpisodes,
                    savedEpisodes = savedEpisodes,
                    followedPodcasts = followedPodcasts,
                    preferredCategories = preferredCategories,
                    selectableCategories = selectableCategories,
                    section = section,
                ) as LibraryState
            } else {
                LibraryState.Success(
                    findQuery = query,
                    allPlayedEpisodes = result.playingEpisodes,
                    likedEpisodes = result.likedEpisodes,
                    savedEpisodes = result.savedEpisodes,
                    followedPodcasts = result.followedPodcasts,
                    preferredCategories = emptyList(),
                    selectableCategories = selectableCategories,
                    section = section,
                ) as LibraryState
            }
        }.catch { e ->
            Timber.e(e, "보관함 데이터를 불러오지 못했다")
            emit(LibraryState.Error(e.asDataError()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryState.Loading,
    )

    private val _action = MutableSharedFlow<LibraryAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<LibraryEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    // OPML 진행률은 일부러 위 state 체인에 얹지 않는다. state 는
    // stateIn(WhileSubscribed(5_000)) 라 탭을 5초 넘게 비우면 그 순간 구독이 끊기고,
    // combine 안에 진행률을 넣으면 import 도 같이 멈춘다(ViewModel 은 죽지 않는데 flow
    // 수집만 죽는다). 그래서 별도 StateFlow 로 두고 collect 결과를 직접 밀어 넣는다.
    private val _opmlProgress = MutableStateFlow<OpmlImportProgress?>(null)
    val opmlProgress: StateFlow<OpmlImportProgress?> = _opmlProgress.asStateFlow()

    // 완료 통지도 Effect(_effect) 로 보내지 않는다. _effect 는 replay=0, buffer=1 이라
    // 구독자가 없는 사이 emit 되면 그대로 사라진다 — 탭을 옮긴 사이 import 가 끝나면
    // 결과가 증발한다. 완료 여부는 OpmlImportProgress.isFinished 로 상태에 남긴다.

    /** 진행 중인 import. 같은 작업이 두 번 얹히는 것을 막는 데만 쓴다. */
    private var opmlImportJob: Job? = null

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is LibraryAction.QueryChanged -> changeQuery(action.query)
                is LibraryAction.ClickFind -> changeQuery(action.query)
                is LibraryAction.ClearQuery -> clearQuery()
                is LibraryAction.ClickPlayingEpisode -> resumeEpisode(action.episode)
                is LibraryAction.ClickEpisode -> playEpisode(action.episode)
                is LibraryAction.ClickPodcast -> clickPodcast(action.podcast)
                is LibraryAction.ToggleLikedEpisode -> toggleLikedEpisode(action.episode)
                is LibraryAction.ToggleSavedEpisode -> toggleSavedEpisode(action.episode)
                is LibraryAction.ToggleFollowedPodcast -> toggleFollowedPodcast(action.podcast)
                is LibraryAction.TogglePreferredCategory -> toggleCategory(action.category)
                is LibraryAction.SelectSection -> selectSection(action.section)
                is LibraryAction.Retry -> retry()
                is LibraryAction.RequestOpmlExport -> requestOpmlExport()
                is LibraryAction.ExportOpml -> exportOpml(action.destinationUri)
                is LibraryAction.ImportOpml -> importOpml(action.sourceUri)
                // 진행 중에는 지우지 않는다 — 진행 중이면 시트가 다시 뜨는 조건이라, 지워도
                // 곧 다음 progress 로 값이 덮인다. 완료된 결과만 지운다.
                is LibraryAction.DismissOpmlProgress -> {
                    if (_opmlProgress.value?.isFinished == true) _opmlProgress.value = null
                }
            }
        }
    }

    fun sendAction(action: LibraryAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun changeQuery(query: String) = viewModelScope.launch {
        _findQuery.emit(query)
    }

    private fun clearQuery() = viewModelScope.launch {
        _findQuery.emit("")
    }

    private fun resumeEpisode(playedEpisode: Episode) = viewModelScope.launch {
        if (playedEpisode.isCompleted) {
            playEpisodeUseCase(playedEpisode)
        } else {
            resumeEpisodeUseCase(playedEpisode)
        }
    }

    private fun playEpisode(episode: Episode) = viewModelScope.launch {
        playEpisodeUseCase(episode)
    }

    private fun clickPodcast(podcast: Podcast) = viewModelScope.launch {
        _effect.emit(LibraryEffect.NavigateToPodcast(podcast.id))
    }

    private fun toggleLikedEpisode(episode: Episode) = viewModelScope.launch {
        toggleLikedEpisodeUseCase(episode)
    }

    private fun toggleSavedEpisode(episode: Episode) = viewModelScope.launch {
        val isSavedNow = saveEpisodeUseCase(episode)
        if (!isSavedNow) {
            _effect.emit(LibraryEffect.ShowUnsaveSnackbar(episode))
        }
    }

    private fun toggleFollowedPodcast(followedPodcast: Podcast) = viewModelScope.launch {
        toggleFollowedUseCase(followedPodcast.id)
    }

    private fun toggleCategory(category: Category) = viewModelScope.launch {
        toggleCategoryUseCase(category)
    }

    private fun selectSection(section: LibrarySection) = viewModelScope.launch {
        _section.emit(section)
    }

    private fun retry() {
        retryTrigger.update { it + 1 }
    }

    /**
     * 파일 선택기를 열기 전에 내보낼 것이 있는지 먼저 본다.
     *
     * SAF 의 `CreateDocument` 는 **URI 를 돌려주기 전에 이미 파일을 만든다.** 선택기를 먼저
     * 열고 나서 "팔로우가 없다" 를 알리면, 알림과 별개로 사용자의 다운로드 폴더에는 0바이트
     * 짜리 .opml 이 남는다. 실제로 그렇게 남는 것을 확인하고 이 단계를 넣었다.
     */
    private fun requestOpmlExport() = viewModelScope.launch {
        try {
            if (getFollowedPodcastsOnceUseCase().isEmpty()) {
                _effect.emit(LibraryEffect.ShowOpmlEmpty)
            } else {
                _effect.emit(LibraryEffect.LaunchOpmlExport)
            }
        } catch (e: Exception) {
            // 이 조회가 던지면 선택기도 못 열고 알림도 없이 끝난다. 사용자에게는 버튼이
            // 먹지 않는 것으로만 보이므로, 아래 exportOpml 과 같은 실패로 접어 알린다.
            Timber.e(e, "팔로우 목록을 읽지 못해 OPML 내보내기를 시작하지 못했다")
            _effect.emit(LibraryEffect.ShowOpmlExportFailed)
        }
    }

    private fun exportOpml(destinationUri: String) = viewModelScope.launch {
        try {
            val count = exportOpmlUseCase(destinationUri)
            if (count == 0) {
                // requestOpmlExport 가 이미 걸렀으므로 여기까지 오는 일은 거의 없다.
                // 확인과 저장 사이에 마지막 팔로우가 풀린 경우를 위해 남겨 둔다.
                _effect.emit(LibraryEffect.ShowOpmlEmpty)
            } else {
                _effect.emit(LibraryEffect.ShowOpmlExported(count))
            }
        } catch (e: Exception) {
            Timber.e(e, "OPML 내보내기에 실패했다")
            _effect.emit(LibraryEffect.ShowOpmlExportFailed)
        }
    }

    private fun importOpml(sourceUri: String) {
        // 이미 진행 중이면 새 import 를 얹지 않는다 — 두 Job 이 같은 _opmlProgress 를
        // 동시에 밀어 넣으면 어느 쪽 진행률인지 알 수 없어진다.
        //
        // 판정은 진행률이 아니라 **Job** 으로 한다. 진행률은 파일을 읽고 첫 emit 이
        // 나온 뒤에야 채워지므로, 그 사이에 사용자가 파일을 한 번 더 고르면 진행률만
        // 보는 판정은 둘 다 통과시킨다.
        if (opmlImportJob?.isActive == true) return

        // 지난번 결과를 먼저 지운다. ImportOpmlUseCase 의 첫 방출은 파일을 다 읽은 뒤에야
        // 나오는데, 그동안 이전 가져오기의 isFinished 결과가 남아 있으면 시트가 새 작업이
        // 아니라 **지난 결과**를 그리고 두 버튼도 눌리는 채로 있다.
        _opmlProgress.value = null

        opmlImportJob = viewModelScope.launch {
            importOpmlUseCase(sourceUri)
                .catch { e ->
                    // 파일을 읽지 못한 경우(권한 취소, 손상된 파일 등)는 진행률로 표현할
                    // 수 없는 예외라 여기서만 잡아 Effect 로 낸다.
                    Timber.e(e, "OPML 파일을 읽지 못했다")
                    _opmlProgress.value = null
                    _effect.emit(LibraryEffect.ShowOpmlImportFailed)
                }
                .collect { progress ->
                    _opmlProgress.value = progress
                }
        }
    }

    companion object {
        private const val SECTION_MAX = 10
    }
}

sealed interface LibraryState {
    data object Loading : LibraryState
    data class Success(
        val findQuery: String,
        val allPlayedEpisodes: List<Episode>,
        val likedEpisodes: List<Episode>,
        val savedEpisodes: List<Episode>,
        val followedPodcasts: List<Podcast>,
        val preferredCategories: List<Category>,
        val selectableCategories: List<SelectableCategory>,
        val section: LibrarySection,
    ) : LibraryState

    data class Error(val error: DataError) : LibraryState
}

sealed interface LibraryAction {
    data class QueryChanged(val query: String) : LibraryAction
    data class ClickFind(val query: String) : LibraryAction
    data object ClearQuery : LibraryAction
    data class ClickPlayingEpisode(val episode: Episode) : LibraryAction
    data class ClickEpisode(val episode: Episode) : LibraryAction
    data class ClickPodcast(val podcast: Podcast) : LibraryAction
    data class ToggleLikedEpisode(val episode: Episode) : LibraryAction
    data class ToggleSavedEpisode(val episode: Episode) : LibraryAction
    data class ToggleFollowedPodcast(val podcast: Podcast) : LibraryAction
    data class TogglePreferredCategory(val category: Category) : LibraryAction
    data class SelectSection(val section: LibrarySection) : LibraryAction
    data object Retry : LibraryAction
    /** 내보내기 요청. 내보낼 것이 있는지 확인한 뒤에야 파일 선택기가 열린다. */
    data object RequestOpmlExport : LibraryAction
    data class ExportOpml(val destinationUri: String) : LibraryAction
    data class ImportOpml(val sourceUri: String) : LibraryAction

    /** 완료된 진행률을 지운다. 시트를 다시 열었을 때 지난 결과가 그대로 남아 있지 않게 한다. */
    data object DismissOpmlProgress : LibraryAction
}

sealed interface LibraryEffect {
    data class NavigateToPodcast(val podcastId: Long) : LibraryEffect
    data class ShowUnsaveSnackbar(val episode: Episode) : LibraryEffect
    /** 내보낼 것이 있으니 파일 선택기를 열어도 된다. */
    data object LaunchOpmlExport : LibraryEffect
    data class ShowOpmlExported(val count: Int) : LibraryEffect
    data object ShowOpmlExportFailed : LibraryEffect

    /** 팔로우한 팟캐스트가 0개라 내보낼 것이 없다. */
    data object ShowOpmlEmpty : LibraryEffect

    /** import 진행률로는 표현할 수 없는, 파일 자체를 읽지 못한 실패. */
    data object ShowOpmlImportFailed : LibraryEffect
}

enum class LibrarySection { All, RecentlyListened, Liked, Saved, Followed, Preferred; }

sealed interface SeparatedUiModel<out T> {
    data class Separator(val label: String) : SeparatedUiModel<Nothing>
    data class Content<T>(val data: T) : SeparatedUiModel<T>
}