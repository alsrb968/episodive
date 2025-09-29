package io.jacob.episodive.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object HomeBaseRoute

fun NavController.navigateToHome(navOptions: NavOptions) =
    navigate(route = HomeBaseRoute, navOptions)

private fun NavGraphBuilder.homeScreen(
    onPodcatClick: (Podcast) -> Unit,
//    onStoryClick: (Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    composable<HomeRoute> {
        HomeRoute(
            onPodcastClick = onPodcatClick,
//            onStoryClick = onStoryClick,
            onShowSnackbar = onShowSnackbar
        )
    }
}

@Composable
private fun HomeNavHost(
    navController: NavHostController,
    navigateToPodcast: NavController.(Podcast) -> Unit,
//    navigateToStoryDetail: NavController.(Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    destination: NavGraphBuilder.(NavController) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        homeScreen(
            onPodcatClick = { navController.navigateToPodcast(it) },
//            onStoryClick = { navController.navigateToStoryDetail(it) },
            onShowSnackbar = onShowSnackbar,
        )

        destination(navController)
    }
}

fun NavGraphBuilder.homeSection(
    onRegisterNestedNavController: (NavHostController) -> Unit,
    navigateToPodcast: NavController.(Podcast) -> Unit,
//    navigateToStoryDetail: NavController.(Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    destination: NavGraphBuilder.(NavController) -> Unit,
) {
    composable<HomeBaseRoute> {
        val navController = rememberNavController()

        LaunchedEffect(navController) {
            onRegisterNestedNavController(navController)
        }

        HomeNavHost(
            navController = navController,
            navigateToPodcast = navigateToPodcast,
//            navigateToStoryDetail = navigateToStoryDetail,
            onShowSnackbar = onShowSnackbar,
            destination = destination
        )
    }
}