package io.jacob.episodive

import android.content.Intent
import android.net.Uri
import app.cash.turbine.test
import io.jacob.episodive.core.domain.usecase.user.GetUserDataUseCase
import io.jacob.episodive.core.model.share.EpisodiveDeepLink
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.jacob.episodive.sync.EpisodeSyncNotificationHelper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUserDataUseCase = mockk<GetUserDataUseCase>(relaxed = true)

    private val viewModel = MainActivityViewModel(
        getUserDataUseCase = getUserDataUseCase,
    )

    @Test
    fun `Given intent with podcast_id, When handleDeepLink, Then emits Podcast event`() =
        runTest {
            // Given
            val intent = mockk<Intent> {
                every { data } returns null
                every { getLongExtra(EpisodeSyncNotificationHelper.EXTRA_PODCAST_ID, -1L) } returns 42L
            }

            // When & Then
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(intent)
                val event = awaitItem()
                assertEquals(DeepLinkEvent.Podcast(42L), event)
                cancel()
            }
        }

    @Test
    fun `Given intent without podcast_id, When handleDeepLink, Then does not emit`() =
        runTest {
            // Given
            val intent = mockk<Intent> {
                every { data } returns null
                every { getLongExtra(EpisodeSyncNotificationHelper.EXTRA_PODCAST_ID, -1L) } returns -1L
                every { getBooleanExtra(MainActivity.EXTRA_WIDGET_OPEN_PLAYER, false) } returns false
            }

            // When & Then
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(intent)
                expectNoEvents()
                cancel()
            }
        }

    @Test
    fun `Given intent with open_player, When handleDeepLink, Then emits Player event`() =
        runTest {
            // Given
            val intent = mockk<Intent> {
                every { data } returns null
                every { getLongExtra(EpisodeSyncNotificationHelper.EXTRA_PODCAST_ID, -1L) } returns -1L
                every { getBooleanExtra(MainActivity.EXTRA_WIDGET_OPEN_PLAYER, false) } returns true
                every { removeExtra(MainActivity.EXTRA_WIDGET_OPEN_PLAYER) } just Runs
            }

            // When & Then
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(intent)
                assertEquals(DeepLinkEvent.Player, awaitItem())
                cancel()
            }
        }

    @Test
    fun `Given null intent, When handleDeepLink, Then does not emit`() =
        runTest {
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(null)
                expectNoEvents()
                cancel()
            }
        }

    // --- episodive:// 커스텀 스킴 ---

    /**
     * URI 로 들어온 Intent 에는 알림·위젯 extra 가 실리지 않는다. 그 스텁을 두지 않는 것이
     * 곧 "URI 분기가 먼저 끝나고 아래로 내려가지 않는다"는 계약이다 — 내려가면 mockk 가
     * 스텁 없는 호출에 예외를 던져 테스트가 깨진다.
     */
    private fun deepLinkIntent(uri: String) = mockk<Intent> {
        every { data } returns Uri.parse(uri)
    }

    @Test
    fun `Given podcast deep link, When handleDeepLink, Then emits Podcast event`() =
        runTest {
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(deepLinkIntent("episodive://podcast/42"))
                assertEquals(DeepLinkEvent.Podcast(42L), awaitItem())
                cancel()
            }
        }

    @Test
    fun `Given episode deep link, When handleDeepLink, Then emits Episode event`() =
        runTest {
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(deepLinkIntent("episodive://episode/7"))
                assertEquals(
                    DeepLinkEvent.Episode(EpisodiveDeepLink.Episode(7L)),
                    awaitItem(),
                )
                cancel()
            }
        }

    @Test
    fun `Given episode deep link with a timestamp, When handleDeepLink, Then carries it in millis`() =
        runTest {
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(deepLinkIntent("episodive://episode/7?t=83"))
                val event = awaitItem() as DeepLinkEvent.Episode
                assertEquals(83_000L, event.target.startPositionMs)
                cancel()
            }
        }

    @Test
    fun `Given a clip deep link, When handleDeepLink, Then carries both start and duration`() =
        runTest {
            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(deepLinkIntent("episodive://episode/7?t=83&d=45"))
                val event = awaitItem() as DeepLinkEvent.Episode
                assertEquals(83_000L, event.target.startPositionMs)
                assertEquals(45_000L, event.target.clipDurationMs)
                cancel()
            }
        }

    @Test
    fun `Given an unknown uri, When handleDeepLink, Then falls through to the extra branches`() =
        runTest {
            // 우리 스킴이 아니면 URI 분기를 지나쳐 extra 를 본다. 그 extra 도 비어 있으므로
            // 아무것도 방출하지 않는다.
            val intent = mockk<Intent> {
                every { data } returns Uri.parse("https://example.com/episode/7")
                every { getLongExtra(EpisodeSyncNotificationHelper.EXTRA_PODCAST_ID, -1L) } returns -1L
                every { getBooleanExtra(MainActivity.EXTRA_WIDGET_OPEN_PLAYER, false) } returns false
            }

            viewModel.deepLinkEvent.test {
                viewModel.handleDeepLink(intent)
                expectNoEvents()
                cancel()
            }
        }

    @Test
    fun `When consumeDeepLink, Then replay cache is cleared`() =
        runTest {
            // Given - emit an event
            val intent = mockk<Intent> {
                every { data } returns null
                every { getLongExtra(EpisodeSyncNotificationHelper.EXTRA_PODCAST_ID, -1L) } returns 42L
            }
            viewModel.handleDeepLink(intent)

            // When
            viewModel.consumeDeepLink()

            // Then - new subscriber should not receive the event
            viewModel.deepLinkEvent.test {
                expectNoEvents()
                cancel()
            }
        }
}
