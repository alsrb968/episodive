package io.jacob.episodive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import io.jacob.episodive.feature.clip.navigation.clipSection
import io.jacob.episodive.feature.home.navigation.homeSection
import io.jacob.episodive.feature.library.navigation.librarySection
import io.jacob.episodive.feature.podcast.navigation.navigateToPodcast
import io.jacob.episodive.feature.podcast.navigation.podcastScreen
import io.jacob.episodive.feature.search.navigation.searchSection
import io.jacob.episodive.ui.EpisodiveAppState

@Composable
fun EpisodiveNavHost(
    modifier: Modifier = Modifier,
    appState: EpisodiveAppState,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    NavHost(
        navController = appState.navController,
        startDestination = appState.startDestination.baseRoute,
        modifier = modifier,
    ) {
        homeSection(
            onRegisterNestedNavController = { navController ->
                appState.registerNestedNavController(BottomBarDestination.HOME, navController)
            },
            navigateToPodcast = { navigateToPodcast(it) },
//            navigateToStoryDetail = { navigateToStoryDetail(it) },
            onShowSnackbar = onShowSnackbar
        ) { nestedNavController ->
            addDetailsGraph(
                navController = nestedNavController,
                onShowSnackbar = onShowSnackbar
            )
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

fun NavGraphBuilder.addDetailsGraph(
    navController: NavController,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    podcastScreen(
        onBackClick = navController::popBackStack,
        onShowSnackbar = onShowSnackbar
    )
}