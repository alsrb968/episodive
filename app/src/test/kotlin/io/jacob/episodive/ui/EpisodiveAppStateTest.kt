package io.jacob.episodive.ui

import androidx.compose.ui.test.junit4.createComposeRule
import io.jacob.episodive.MainActivityViewModel
import io.jacob.episodive.core.data.util.NetworkMonitor
import io.jacob.episodive.feature.home.navigation.HomeRoute
import io.jacob.episodive.feature.search.navigation.SearchRoute
import io.jacob.episodive.navigation.BottomBarDestination
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 홈의 검색 바로가기가 만드는 신호의 수명을 지키는 계약 테스트.
 *
 * 화면 단위 테스트(`HomeScreenTest`·`SearchScreenTest`)는 "아이콘을 누르면 콜백이 온다",
 * "신호가 켜져 있으면 포커스가 잡힌다" 까지만 본다. 그 사이를 잇는 네비게이션과 신호 수명은
 * 여기서만 드러난다.
 */
@RunWith(RobolectricTestRunner::class)
class EpisodiveAppStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val networkMonitor = object : NetworkMonitor {
        override val isOnline: Flow<Boolean> = flowOf(true)
    }

    private val viewModel = mockk<MainActivityViewModel>(relaxed = true)

    private lateinit var appState: EpisodiveAppState

    private fun setUpAppState() {
        composeTestRule.setContent {
            appState = rememberEpisodiveAppState(
                networkMonitor = networkMonitor,
                viewModel = viewModel,
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun searchShortcut_movesToSearchTab() {
        setUpAppState()

        composeTestRule.runOnIdle { appState.navigateToSearchWithFocus() }

        composeTestRule.runOnIdle {
            assertEquals(SearchRoute, appState.navigationState.topLevelRoute)
        }
    }

    @Test
    fun searchShortcut_raisesFocusSignal() {
        setUpAppState()

        composeTestRule.runOnIdle { appState.navigateToSearchWithFocus() }

        composeTestRule.runOnIdle { assertTrue(appState.searchAutoFocus) }
    }

    @Test
    fun searchShortcut_rewindsSearchTabToItsRoot() {
        // 검색하다 팟캐스트 상세로 파고든 채 탭을 떠나면 그 화면이 검색 탭 스택에 남는다.
        // 탭 전환만 하면 검색창 대신 그 상세가 되살아나므로, 위에 쌓인 것을 걷어내야 한다.
        setUpAppState()

        composeTestRule.runOnIdle {
            appState.navigateToBottomBarDestination(BottomBarDestination.SEARCH)
            appState.navigateToPodcast(podcastId = 42L)
        }
        composeTestRule.runOnIdle {
            appState.navigateToBottomBarDestination(BottomBarDestination.HOME)
        }

        composeTestRule.runOnIdle { appState.navigateToSearchWithFocus() }

        composeTestRule.runOnIdle {
            assertEquals(SearchRoute, appState.navigationState.topLevelRoute)
            assertEquals(1, appState.navigationState.backStacks[SearchRoute]?.size)
        }
    }

    @Test
    fun bottomBarSearch_doesNotRaiseFocusSignal() {
        // 하단 바로 들어가는 것은 둘러보러 가는 길이다. 키보드가 따라 올라오면 안 된다.
        setUpAppState()

        composeTestRule.runOnIdle {
            appState.navigateToBottomBarDestination(BottomBarDestination.SEARCH)
        }

        composeTestRule.runOnIdle {
            assertEquals(SearchRoute, appState.navigationState.topLevelRoute)
            assertFalse(appState.searchAutoFocus)
        }
    }

    @Test
    fun consumeSearchAutoFocus_clearsSignal() {
        setUpAppState()

        composeTestRule.runOnIdle { appState.navigateToSearchWithFocus() }
        composeTestRule.runOnIdle { appState.consumeSearchAutoFocus() }

        composeTestRule.runOnIdle { assertFalse(appState.searchAutoFocus) }
    }

    @Test
    fun leavingSearchTabBeforeConsuming_discardsSignal() {
        // 검색이 로딩이라 검색바가 아직 없는 사이에 다른 탭으로 가버린 상황. 신호가 남으면
        // 나중에 하단 바로 검색에 들어올 때 부르지 않은 키보드가 올라온다.
        setUpAppState()

        composeTestRule.runOnIdle { appState.navigateToSearchWithFocus() }
        composeTestRule.runOnIdle {
            appState.navigateToBottomBarDestination(BottomBarDestination.HOME)
            appState.discardSearchAutoFocusIfTabLeft()
        }

        composeTestRule.runOnIdle {
            assertEquals(HomeRoute, appState.navigationState.topLevelRoute)
            assertFalse(appState.searchAutoFocus)
        }
    }

    @Test
    fun stayingOnSearchTab_keepsSignalUntilConsumed() {
        // 같은 탭에 머무는 동안에는 신호가 살아 있어야 한다. 검색이 로딩을 끝내고 입력창을
        // 그리는 순간에 쓰일 값이다.
        setUpAppState()

        composeTestRule.runOnIdle { appState.navigateToSearchWithFocus() }
        composeTestRule.runOnIdle { appState.discardSearchAutoFocusIfTabLeft() }

        composeTestRule.runOnIdle { assertTrue(appState.searchAutoFocus) }
    }
}
