package io.jacob.episodive.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecommendedPodcastsPagingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.ToggleFollowedUseCase
import io.jacob.episodive.core.domain.usecase.user.GetPreferredCategoriesUseCase
import io.jacob.episodive.core.domain.usecase.user.SetFirstLaunchOffUseCase
import io.jacob.episodive.core.domain.usecase.user.ToggleCategoryUseCase
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.model.asDataError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setFirstLaunchOffUseCase: SetFirstLaunchOffUseCase,
    private val toggleCategoryUseCase: ToggleCategoryUseCase,
    private val toggleFollowedUseCase: ToggleFollowedUseCase,
    private val getPreferredCategoriesUseCase: GetPreferredCategoriesUseCase,
    getFollowedPodcastsUseCase: GetFollowedPodcastsUseCase,
    getUserRecommendedPodcastsPagingUseCase: GetUserRecommendedPodcastsPagingUseCase,
) : ViewModel() {

    private val _page = MutableStateFlow(OnboardingPage.Welcome)
    private val _categories: Flow<List<SelectableCategory>> =
        getPreferredCategoriesUseCase().flatMapLatest { preferredCategories ->
            Timber.d("preferredCategories: $preferredCategories")
            Category.entries.map { category ->
                SelectableCategory(
                    category = category,
                    isSelected = preferredCategories.contains(category),
                )
            }.let { flowOf(it) }
        }
    val recommendedPodcasts: Flow<PagingData<Podcast>> = _page
        .flatMapLatest { page ->
            if (page == OnboardingPage.PodcastSelection) {
                getUserRecommendedPodcastsPagingUseCase(max = 50)
            } else {
                // 여기의 무인자 empty() 는 의도한 것이다. 다른 페이지에 있는 동안의 자리
                // 지킴이일 뿐이라 로드 상태를 Loading 에 남겨 둬야 한다. EmptyLoadStates 로
                // 바꾸면, 페이저가 팟캐스트 선택 페이지를 조합하는 시점(스와이프 도중)과
                // _page 갱신 사이의 몇 프레임 동안 cachedIn 이 이 값을 되돌려 주고 화면이
                // 스켈레톤 대신 "비어 있음"을 한 번 깜빡인다.
                flowOf(PagingData.empty())
            }
        }.cachedIn(viewModelScope)

    // 팔로우 상태는 PagingData 를 무효화하지 않고 로컬 오버레이로만 반영한다.
    // (팔로우 토글이 리스트 전체를 refresh 시켜 스크롤이 튀는 문제 방지)
    // state 와 별개의 stateIn 체인이라 실패해도 전체 화면을 Error 로 떨어뜨리지 않는다 — 대신
    // 로그를 남기고 빈 집합으로 폴백해, 팟캐스트 선택 화면은 "전부 미팔로우"로만 보이게 한다.
    val followedPodcastIds: StateFlow<Set<Long>> =
        getFollowedPodcastsUseCase(max = Int.MAX_VALUE)
            .map { podcasts -> podcasts.map { it.id }.toSet() }
            .catch { e ->
                Timber.e(e, "Failed to load followed podcast ids")
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet(),
            )

    // 재시도 액션이 들어오면 값을 증가시켜 아래 state 체인을 처음부터 다시 구독하게 한다.
    private val retryTrigger = MutableStateFlow(0)

    val state: StateFlow<OnboardingState> = retryTrigger.flatMapLatest {
        // catch 를 flatMapLatest 안쪽(시도 1회 단위)에 둬야 한다. 바깥에 두면 예외를 잡은 뒤
        // 전체 flow(retryTrigger 포함)가 완료돼 버려, 그 다음 retryTrigger 갱신이 와도 다시
        // 구독을 시작할 수 없다 — retryTrigger 자체는 끝나지 않는 flow라 재시도가 먹통이 된다.
        _categories.map { categories ->
            OnboardingState.Success(
                categories = categories,
            ) as OnboardingState
        }.catch { e ->
            Timber.e(e, "온보딩 카테고리를 불러오지 못했다")
            emit(OnboardingState.Error(e.asDataError()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OnboardingState.Loading
    )

    private val _action = MutableSharedFlow<OnboardingAction>(extraBufferCapacity = 1)

    private val _effect = MutableSharedFlow<OnboardingEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        handleActions()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is OnboardingAction.NextPage -> nextPage()
                is OnboardingAction.PreviousPage -> previousPage()
                is OnboardingAction.ChooseCategory -> chooseCategory(action.category)
                is OnboardingAction.ChoosePodcast -> choosePodcast(action.podcast)
                is OnboardingAction.Retry -> retryTrigger.update { it + 1 }
            }
        }
    }

    fun sendAction(action: OnboardingAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun nextPage() = viewModelScope.launch {
        when (_page.value) {
            OnboardingPage.CategorySelection -> {
                if (getPreferredCategoriesUseCase().first().size < 3) {
                    _effect.emit(OnboardingEffect.ToastMoreCategories)
                    return@launch
                }
            }

            OnboardingPage.PodcastSelection -> {
                finishOnboarding()
            }

            else -> {}
        }

        _page.value.next()?.let { _page.emit(it) }
        _effect.emit(OnboardingEffect.MoveToPage(_page.value))
    }

    private fun previousPage() = viewModelScope.launch {
        when (_page.value) {
            OnboardingPage.Welcome,
            OnboardingPage.Completion,
                -> return@launch

            else -> {}
        }

        _page.value.previous()?.let { _page.emit(it) }
        _effect.emit(OnboardingEffect.MoveToPage(_page.value))
    }

    private fun chooseCategory(category: Category) = viewModelScope.launch {
        toggleCategoryUseCase(category)
    }

    private fun choosePodcast(podcast: Podcast) = viewModelScope.launch {
        toggleFollowedUseCase(podcast.id)
    }

    private fun finishOnboarding() = viewModelScope.launch {
        delay(3000L)
        setFirstLaunchOffUseCase()
    }
}

sealed interface OnboardingState {
    data object Loading : OnboardingState
    data class Success(
        val categories: List<SelectableCategory>,
    ) : OnboardingState

    data class Error(val error: DataError) : OnboardingState
}

sealed interface OnboardingAction {
    data object NextPage : OnboardingAction
    data object PreviousPage : OnboardingAction
    data class ChooseCategory(val category: Category) : OnboardingAction
    data class ChoosePodcast(val podcast: Podcast) : OnboardingAction
    data object Retry : OnboardingAction
}

sealed interface OnboardingEffect {
    data object ToastMoreCategories : OnboardingEffect
    data class MoveToPage(val page: OnboardingPage) : OnboardingEffect
}

enum class OnboardingPage {
    Welcome, CategorySelection, PodcastSelection, Completion;

    fun next() = entries.getOrNull(ordinal.plus(1))
    fun previous() = entries.getOrNull(ordinal.minus(1))

    companion object {
        val count = entries.size
        fun fromIndex(index: Int) = entries.getOrNull(index)
        fun firstIndex() = entries.first().ordinal
        fun lastIndex() = entries.last().ordinal
    }
}