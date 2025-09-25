package io.jacob.episodive.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.feed.GetRecommendedFeedsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.ToggleFollowedUseCase
import io.jacob.episodive.core.domain.usecase.user.GetPreferredCategoriesUseCase
import io.jacob.episodive.core.domain.usecase.user.SetFirstLaunchOffUseCase
import io.jacob.episodive.core.domain.usecase.user.ToggleCategoryUseCase
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Feed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setFirstLaunchOffUseCase: SetFirstLaunchOffUseCase,
    private val toggleCategoryUseCase: ToggleCategoryUseCase,
    private val toggleFollowedUseCase: ToggleFollowedUseCase,
    private val getPreferredCategoriesUseCase: GetPreferredCategoriesUseCase,
    private val getRecommendedFeedsUseCase: GetRecommendedFeedsUseCase,
) : ViewModel() {

    private val _page = MutableStateFlow(OnboardingPage.Welcome)
    private val _categories: Flow<List<CategoryUiModel>> =
        getPreferredCategoriesUseCase().flatMapLatest { preferredCategories ->
            Category.entries.map { category ->
                CategoryUiModel(
                    category = category,
                    isSelected = preferredCategories.contains(category),
                )
            }.let { flowOf(it) }
        }
    private val _feeds: Flow<List<FeedUiModel>> =
        getRecommendedFeedsUseCase().flatMapLatest { recommendedFeeds ->
            recommendedFeeds.map { feed ->
                FeedUiModel(
                    feed = feed,
                    isSelected = false,
                )
            }.let { flowOf(it) }
        }

    val state: StateFlow<OnboardingState> = combine(
        _page,
        _categories,
        _feeds
    ) { page, categories, feeds ->
        OnboardingState(
            page = page,
            categories = categories,
            feeds = feeds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OnboardingState(
            page = OnboardingPage.Welcome,
            categories = emptyList(),
            feeds = emptyList(),
        )
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
                is OnboardingAction.ChooseFeed -> chooseFeed(action.feed)
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

            OnboardingPage.FeedSelection -> {
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
            OnboardingPage.Completed -> return@launch

            else -> {}
        }

        _page.value.previous()?.let { _page.emit(it) }
        _effect.emit(OnboardingEffect.MoveToPage(_page.value))
    }

    private fun chooseCategory(category: Category) = viewModelScope.launch {
        toggleCategoryUseCase(category)
    }

    private fun chooseFeed(feed: Feed) = viewModelScope.launch {
        toggleFollowedUseCase(feed.id)
    }

    private fun finishOnboarding() = viewModelScope.launch {
        setFirstLaunchOffUseCase()
        delay(3000L)
        _effect.emit(OnboardingEffect.NavigateToMain)
    }
}

data class OnboardingState(
    val page: OnboardingPage,
    val categories: List<CategoryUiModel>,
    val feeds: List<FeedUiModel>,
)

sealed interface OnboardingAction {
    data object NextPage : OnboardingAction
    data object PreviousPage : OnboardingAction
    data class ChooseCategory(val category: Category) : OnboardingAction
    data class ChooseFeed(val feed: Feed) : OnboardingAction
}

sealed interface OnboardingEffect {
    data object ToastMoreCategories : OnboardingEffect
    data object NavigateToMain : OnboardingEffect
    data class MoveToPage(val page: OnboardingPage) : OnboardingEffect
}

enum class OnboardingPage {
    Welcome, CategorySelection, FeedSelection, Completed;

    fun next() = entries.getOrNull(ordinal.plus(1))
    fun previous() = entries.getOrNull(ordinal.minus(1))

    companion object {
        val count = entries.size
        fun fromIndex(index: Int) = entries.getOrNull(index)
    }
}

data class CategoryUiModel(
    val category: Category,
    val isSelected: Boolean,
)

data class FeedUiModel(
    val feed: Feed,
    val isSelected: Boolean,
)