package io.jacob.episodive.feature.library.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.jacob.episodive.feature.library.LibraryRoute
import kotlinx.serialization.Serializable

@Serializable
data object LibraryRoute

@Serializable
data object LibraryBaseRoute

fun NavController.navigateToLibrary(navOptions: NavOptions) =
    navigate(route = LibraryBaseRoute, navOptions)

private fun NavGraphBuilder.libraryScreen(
//    onPlaceClick: (Place) -> Unit,
//    onStoryClick: (Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    composable<LibraryRoute> {
        LibraryRoute(
//            onPlaceClick = onPlaceClick,
//            onStoryClick = onStoryClick,
            onShowSnackbar = onShowSnackbar
        )
    }
}

@Composable
private fun LibraryNavHost(
    navController: NavHostController,
//    navigateToPlaceDetail: NavController.(Place) -> Unit,
//    navigateToStoryDetail: NavController.(Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    destination: NavGraphBuilder.(NavController) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = LibraryRoute
    ) {
        libraryScreen(
//            onPlaceClick = { navController.navigateToPlaceDetail(it) },
//            onStoryClick = { navController.navigateToStoryDetail(it) },
            onShowSnackbar = onShowSnackbar,
        )

        destination(navController)
    }
}

fun NavGraphBuilder.librarySection(
    onRegisterNestedNavController: (NavHostController) -> Unit,
//    navigateToPlaceDetail: NavController.(Place) -> Unit,
//    navigateToStoryDetail: NavController.(Story) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    destination: NavGraphBuilder.(NavController) -> Unit,
) {
    composable<LibraryBaseRoute> {
        val navController = rememberNavController()

        LaunchedEffect(navController) {
            onRegisterNestedNavController(navController)
        }

        LibraryNavHost(
            navController = navController,
//            navigateToPlaceDetail = navigateToPlaceDetail,
//            navigateToStoryDetail = navigateToStoryDetail,
            onShowSnackbar = onShowSnackbar,
            destination = destination
        )
    }
}