package io.jacob.episodive

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.tracing.trace
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.data.util.NetworkMonitor
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.ui.EpisodiveApp
import io.jacob.episodive.ui.rememberEpisodiveAppState
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    @Player(EpisodivePlayers.Main)
    lateinit var playerRepository: PlayerRepository

    private val viewModel: MainActivityViewModel by viewModels()

    private var controllerFuture: ListenableFuture<MediaController>? = null

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.state.value.shouldKeepSplashScreen()
        }

        // Start and bind MediaSessionService
        val intent = Intent(this, MediaNotificationService::class.java)
        startService(intent)

        val sessionToken =
            SessionToken(this, ComponentName(this, MediaNotificationService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            // MediaController connected
        }, MoreExecutors.directExecutor())

        // 액티비티가 다시 만들어질 때는 건너뛴다. playOrPause() 는 토글이라 두 번 불리면
        // 사용자가 켜 달라고 한 재생을 그대로 꺼 버린다. intent.removeExtra 는 방어가 되지 못한다 —
        // 재생성 시 시스템이 원본 Intent 를 다시 실어 주므로 extra 가 되살아난다.
        //
        // 딥링크도 같은 가드 안에 둔다. URI 는 `removeExtra` 로 지울 수조차 없어(intent.data 를
        // 비워도 재생성 때 원본이 다시 온다) 방어할 다른 수단이 없다. 새로 들어오는 링크는
        // onNewIntent 가 받으므로 사용자가 같은 링크를 다시 눌러도 막히지 않는다.
        if (savedInstanceState == null) {
            viewModel.handleDeepLink(getIntent())
            handleWidgetAutoplay(getIntent())
        }

        setContent {
            val appState = rememberEpisodiveAppState(
                networkMonitor = networkMonitor,
                viewModel = viewModel,
            )
            EpisodiveTheme {
                EpisodiveApp(appState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleDeepLink(intent)
        handleWidgetAutoplay(intent)
    }

    // MediaNotificationService 가 @UnstableApi 라 클래스를 참조하는 것만으로 opt-in 이 필요하다.
    // onCreate 처럼 @UnstableApi 를 붙이면 요구가 호출자로 번져 onNewIntent 까지 옮겨 붙는다 —
    // 여기서 소비하고 끝낸다.
    @OptIn(UnstableApi::class)
    private fun handleWidgetAutoplay(intent: Intent) {
        if (!intent.getBooleanExtra(EXTRA_WIDGET_AUTOPLAY, false)) return

        trace("widget.autoplay") {
            val startMs = SystemClock.uptimeMillis()
            val svc = Intent(this, MediaNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc)
            } else {
                startService(svc)
            }
            playerRepository.playOrPause()
            intent.removeExtra(EXTRA_WIDGET_AUTOPLAY)
            val deltaMs = SystemClock.uptimeMillis() - startMs
            Log.d("WidgetPerf", "autoplay deltaMs=$deltaMs")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    companion object {
        // ServiceActions.kt (:feature:widget) 에서 하드코딩된 복제본이 동기화되어야 함.
        const val EXTRA_WIDGET_AUTOPLAY = "widget_autoplay"
        const val EXTRA_WIDGET_OPEN_PLAYER = "widget_open_player"
    }
}