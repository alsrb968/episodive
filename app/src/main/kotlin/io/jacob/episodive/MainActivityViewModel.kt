package io.jacob.episodive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jacob.episodive.core.domain.usecase.user.GetUserDataUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
) : ViewModel() {
    val state: StateFlow<MainActivityState> = getUserDataUseCase().map { userData ->
        if (userData.isFirstLaunch) {
            MainActivityState.FirstLaunch
        } else {
            MainActivityState.NotFirstLaunch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainActivityState.Loading,
    )
}

sealed interface MainActivityState {
    data object Loading : MainActivityState
    data object FirstLaunch : MainActivityState
    data object NotFirstLaunch : MainActivityState

    fun shouldKeepSplashScreen() = this is Loading
    fun isFirstLaunch() = this is FirstLaunch
}