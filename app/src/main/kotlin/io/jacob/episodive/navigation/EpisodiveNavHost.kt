package io.jacob.episodive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import io.jacob.episodive.feature.clip.navigation.clipSection
import io.jacob.episodive.feature.home.navigation.homeSection
import io.jacob.episodive.feature.library.navigation.librarySection
import io.jacob.episodive.feature.onboarding.navigation.OnboardingRoute
import io.jacob.episodive.feature.onboarding.navigation.onboardingScreen
import io.jacob.episodive.feature.search.navigation.searchSection
import io.jacob.episodive.ui.EpisodiveAppState

@Composable
fun EpisodiveNavHost(
    modifier: Modifier = Modifier,
    appState: EpisodiveAppState,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val userData by appState.userData.collectAsStateWithLifecycle()

    NavHost(
        navController = appState.navController,
        startDestination = if (userData.isFirstLaunch) OnboardingRoute else appState.startDestination.baseRoute,
        modifier = modifier,
    ) {
        onboardingScreen(
            onShowSnackbar = onShowSnackbar,
        ) {
            appState.navigateToBottomBarDestination(appState.startDestination)
        }

        homeSection(
            onRegisterNestedNavController = { navController ->
                appState.registerNestedNavController(BottomBarDestination.HOME, navController)
            },
//            navigateToPlaceDetail = { navigateToPlaceDetail(it) },
//            navigateToStoryDetail = { navigateToStoryDetail(it) },
            onShowSnackbar = onShowSnackbar
        ) { nestedNavController ->
//            addDetailsGraph(
//                navController = nestedNavController,
//                onShowSnackbar = onShowSnackbar
//            )
        }

        searchSection(
            onRegisterNestedNavController = { navController ->
                appState.registerNestedNavController(BottomBarDestination.SEARCH, navController)
            },
//            navigateToPlaceDetail = { navigateToPlaceDetail(it) },
//            navigateToStoryDetail = { navigateToStoryDetail(it) },
            onShowSnackbar = onShowSnackbar
        ) { nestedNavController ->
//            addDetailsGraph(
//                navController = nestedNavController,
//                onShowSnackbar = onShowSnackbar
//            )
        }

        librarySection(
            onRegisterNestedNavController = { navController ->
                appState.registerNestedNavController(BottomBarDestination.LIBRARY, navController)
            },
            onShowSnackbar = onShowSnackbar
        ) { nestedNavController ->

        }

        clipSection(
            onRegisterNestedNavController = { navController ->
                appState.registerNestedNavController(BottomBarDestination.CLIP, navController)
            },
            onShowSnackbar = onShowSnackbar
        ) { nestedNavController ->

        }
    }
}