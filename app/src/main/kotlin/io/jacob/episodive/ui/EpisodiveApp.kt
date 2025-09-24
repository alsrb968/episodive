package io.jacob.episodive.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import io.jacob.episodive.R
import io.jacob.episodive.core.designsystem.component.EpisodiveNavigationBar
import io.jacob.episodive.core.designsystem.component.EpisodiveNavigationBarItem
import io.jacob.episodive.navigation.EpisodiveNavHost
import kotlin.reflect.KClass

@Composable
fun EpisodiveApp(
    appState: EpisodiveAppState,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    val notConnectedMessage = stringResource(R.string.not_connected)
    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    EpisodiveApp(
        appState = appState,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun EpisodiveApp(
    appState: EpisodiveAppState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {
    val userData by appState.userData.collectAsStateWithLifecycle()
    val currentDestination by appState.currentDestination
        .collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (!userData.isFirstLaunch) {
                EpisodiveNavigationBar {
                    appState.bottomBarDestinations.forEach { destination ->
                        val selected = currentDestination
                            .isRouteInHierarchy(destination.baseRoute)
                        val text = stringResource(destination.iconTextId)

                        EpisodiveNavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = destination.unselectedIcon,
                                    contentDescription = text
                                )
                            },
                            selectedIcon = {
                                Icon(
                                    imageVector = destination.selectedIcon,
                                    contentDescription = text
                                )
                            },
                            label = {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            },
                            selected = selected,
                            onClick = { appState.navigateToBottomBarDestination(destination) },
                        )
                    }
                }
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.statusBars),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            EpisodiveNavHost(
                appState = appState,
                onShowSnackbar = { message, action ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = action,
                        duration = SnackbarDuration.Short,
                    ) == SnackbarResult.ActionPerformed
                },
            )

//            PlayerMiniBar()
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false