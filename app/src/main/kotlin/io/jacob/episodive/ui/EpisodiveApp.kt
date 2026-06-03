package io.jacob.episodive.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.DeepLinkEvent
import io.jacob.episodive.R
import io.jacob.episodive.core.designsystem.component.EpisodiveBackground
import io.jacob.episodive.core.designsystem.component.EpisodiveSwipeDismissSnackbarHost
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.component.EpisodiveNavigationBar
import io.jacob.episodive.core.designsystem.component.EpisodiveNavigationBarItem
import io.jacob.episodive.feature.onboarding.OnboardingRoute
import io.jacob.episodive.feature.player.PlayerBar
import io.jacob.episodive.navigation.EpisodiveNavHost

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
                duration = SnackbarDuration.Long
            )
        }
    }

    EpisodiveBackground(modifier = modifier) {
        EpisodiveApp(
            appState = appState,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
fun EpisodiveApp(
    appState: EpisodiveAppState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {
    val state by appState.viewModel.state.collectAsStateWithLifecycle()

    val onShowSnackbar: suspend (String, String?) -> Boolean = remember {
        { message: String, action: String? ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = action,
                duration = if (action != null) SnackbarDuration.Long else SnackbarDuration.Short,
            ) == SnackbarResult.ActionPerformed
        }
    }

    if (state.isFirstLaunch()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            OnboardingRoute(
                onShowSnackbar = onShowSnackbar,
            )

            EpisodiveSwipeDismissSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(bottom = 70.dp)
            )
        }
        return
    }

    // 위젯 딥링크 → 플레이어 시트 펼침/접힘 신호. 콜드(fresh onCreate) 타이밍에 일회성 effect 가
    // PlayerBar 구독 전 emit 되어 유실되지 않도록, 상태(시그널) 로 PlayerBar 에 전달한다.
    var expandPlayerSignal by remember { mutableIntStateOf(0) }
    var collapsePlayerSignal by remember { mutableIntStateOf(0) }

    // Deep link handling
    LaunchedEffect(Unit) {
        appState.viewModel.deepLinkEvent.collect { event ->
            when (event) {
                is DeepLinkEvent.Podcast -> {
                    appState.navigateToPodcast(event.id)
                    // 앱 내 플레이어에서 팟캐스트 탭 시와 동일하게, 열린 시트가 가리지 않도록 접는다.
                    collapsePlayerSignal++
                    appState.viewModel.consumeDeepLink()
                }

                is DeepLinkEvent.Player -> {
                    expandPlayerSignal++
                    appState.viewModel.consumeDeepLink()
                }
            }
        }
    }

    // POST_NOTIFICATIONS runtime permission request (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* granted or denied — no action needed */ }

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            EpisodiveNavigationBar {
                appState.bottomBarDestinations.forEach { destination ->
                    val selected = destination.navKey == appState.navigationState.topLevelRoute
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
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.statusBars),
        snackbarHost = {
            EpisodiveSwipeDismissSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(bottom = LocalDimensionTheme.current.playerBarHeight),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            EpisodiveNavHost(
                navigationState = appState.navigationState,
                navigator = appState.navigator,
                onShowSnackbar = onShowSnackbar,
            )

            PlayerBar(
                onPodcastClick = { appState.navigateToPodcast(it) },
                onShowSnackbar = onShowSnackbar,
                expandSignal = expandPlayerSignal,
                collapseSignal = collapsePlayerSignal,
            )
        }
    }
}
