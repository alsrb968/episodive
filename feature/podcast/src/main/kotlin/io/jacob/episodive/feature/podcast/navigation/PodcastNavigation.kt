package io.jacob.episodive.feature.podcast.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.jacob.episodive.feature.podcast.PodcastRoute
import io.jacob.episodive.feature.podcast.PodcastViewModel
import kotlinx.serialization.Serializable

@Serializable
data class PodcastRoute(val id: Long)

fun NavController.navigateToPodcast(
    podcastId: Long, navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(route = PodcastRoute(podcastId), navOptions)

fun NavGraphBuilder.podcastScreen(
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    composable<PodcastRoute> { entry ->
        val id = entry.toRoute<PodcastRoute>().id
        PodcastRoute(
            viewModel = hiltViewModel<PodcastViewModel, PodcastViewModel.Factory>(
                key = "podcast:$id"
            ) { factory ->
                factory.create(id)
            },
            onBackClick = onBackClick,
            onShowSnackbar = onShowSnackbar
        )
    }
}