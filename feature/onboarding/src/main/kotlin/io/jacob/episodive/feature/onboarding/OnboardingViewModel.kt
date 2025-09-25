package io.jacob.episodive.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.domain.usecase.feed.GetRecommendedFeedsUseCase
import io.jacob.episodive.core.domain.usecase.podcast.AddFollowedsUseCase
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Feed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val addFollowedsUseCase: AddFollowedsUseCase,
    private val getRecommendedFeedsUseCase: GetRecommendedFeedsUseCase,
) : ViewModel() {

    private val _page = MutableStateFlow(OnboardingPage.Welcome)
    private val _categories = MutableStateFlow<List<CategoryUiModel>>(emptyList())
    private val _feeds = MutableStateFlow<List<FeedUiModel>>(emptyList())

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

    private val selectedCategories = mutableSetOf<Category>()
    private val selectedFeeds = mutableSetOf<Feed>()

    init {
        handleActions()
        handlePage()
    }

    private fun handleActions() = viewModelScope.launch {
        _action.collectLatest { action ->
            when (action) {
                is OnboardingAction.NextPage -> nextPage()
                is OnboardingAction.PreviousPage -> previousPage()
                is OnboardingAction.ChooseCategory ->
                    chooseCategory(action.category, action.isSelected)

                is OnboardingAction.ChooseFeed ->
                    chooseFeed(action.feed, action.isSelected)
            }
        }
    }

    fun sendAction(action: OnboardingAction) = viewModelScope.launch {
        _action.emit(action)
    }

    private fun handlePage() = viewModelScope.launch {
        _page.collectLatest { page ->
            when (page) {
                OnboardingPage.CategorySelection -> _categories.emit(loadCategoryUiModels())
                OnboardingPage.FeedSelection -> _feeds.emit(loadFeedUiModels())
                else -> {}
            }
        }
    }

    private fun nextPage() = viewModelScope.launch {
        when (_page.value) {
            OnboardingPage.CategorySelection -> {
                if (selectedCategories.size < 3) {
                    _effect.emit(OnboardingEffect.ToastMoreCategories)
                    return@launch
                }
                userRepository.setCategories(selectedCategories.toList())
            }

            OnboardingPage.FeedSelection -> {
                addFollowedsUseCase(selectedFeeds.map { it.id })
                finishOnboarding()
            }

            else -> {}
        }

        _page.value.next()?.let { _page.emit(it) }
    }

    private fun previousPage() = viewModelScope.launch {
        when (_page.value) {
            OnboardingPage.Welcome,
            OnboardingPage.Completed -> return@launch

            else -> {}
        }

        _page.value.previous()?.let { _page.emit(it) }
    }

    private fun chooseCategory(category: Category, isSelected: Boolean) {
        if (isSelected) {
            selectedCategories.add(category)
        } else {
            selectedCategories.remove(category)
        }
    }

    private fun chooseFeed(feed: Feed, isSelected: Boolean) {
        if (isSelected) {
            selectedFeeds.add(feed)
        } else {
            selectedFeeds.remove(feed)
        }
    }

    private fun loadCategoryUiModels(): List<CategoryUiModel> {
        return Category.entries.map { category ->
            CategoryUiModel(
                category = category,
                isSelected = selectedCategories.contains(category),
            )
        }
    }

    private suspend fun loadFeedUiModels(): List<FeedUiModel> {
        return getRecommendedFeedsUseCase().first().map { feed ->
            FeedUiModel(
                feed = feed,
                isSelected = selectedFeeds.contains(feed),
            )
        }
    }

    private fun finishOnboarding() = viewModelScope.launch {
        userRepository.setFirstLaunch(false)
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
    data class ChooseCategory(val category: Category, val isSelected: Boolean) : OnboardingAction
    data class ChooseFeed(val feed: Feed, val isSelected: Boolean) : OnboardingAction
}

sealed interface OnboardingEffect {
    data object ToastMoreCategories : OnboardingEffect
    data object NavigateToMain : OnboardingEffect
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