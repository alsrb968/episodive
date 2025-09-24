package io.jacob.episodive.feature.search.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.jacob.episodive.feature.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute

@Serializable
data object SearchBaseRoute

fun NavController.navigateToSearch(navOptions: NavOptions) =
    navigate(route = SearchBaseRoute, navOptions)

private fun NavGraphBuilder.searchScreen(
//    onPlaceClick: (Place) -> Unit,
//    onStoryClick: (Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    composable<SearchRoute> {
        SearchRoute(
//            onPlaceClick = onPlaceClick,
//            onStoryClick = onStoryClick,
            onShowSnackbar = onShowSnackbar
        )
    }
}

@Composable
private fun SearchNavHost(
    navController: NavHostController,
//    navigateToPlaceDetail: NavController.(Place) -> Unit,
//    navigateToStoryDetail: NavController.(Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    destination: NavGraphBuilder.(NavController) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = SearchRoute
    ) {
        searchScreen(
//            onPlaceClick = { navController.navigateToPlaceDetail(it) },
//            onStoryClick = { navController.navigateToStoryDetail(it) },
            onShowSnackbar = onShowSnackbar,
        )

        destination(navController)
    }
}

fun NavGraphBuilder.searchSection(
    onRegisterNestedNavController: (NavHostController) -> Unit,
//    navigateToPlaceDetail: NavController.(Place) -> Unit,
//    navigateToStoryDetail: NavController.(Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    destination: NavGraphBuilder.(NavController) -> Unit,
) {
    composable<SearchBaseRoute> {
        val navController = rememberNavController()

        LaunchedEffect(navController) {
            onRegisterNestedNavController(navController)
        }

        SearchNavHost(
            navController = navController,
//            navigateToPlaceDetail = navigateToPlaceDetail,
//            navigateToStoryDetail = navigateToStoryDetail,
            onShowSnackbar = onShowSnackbar,
            destination = destination
        )
    }
}