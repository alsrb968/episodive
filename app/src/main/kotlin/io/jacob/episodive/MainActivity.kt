package io.jacob.episodive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import io.jacob.episodive.core.data.util.NetworkMonitor
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.ui.EpisodiveApp
import io.jacob.episodive.ui.rememberEpisodiveAppState
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        splashScreen.setKeepOnScreenCondition {
//            isDone.value
//        }
        setContent {
            val appState = rememberEpisodiveAppState(
                networkMonitor = networkMonitor,
                userRepository = userRepository,
            )
            EpisodiveTheme {
                EpisodiveApp(appState)
            }
        }
    }
}