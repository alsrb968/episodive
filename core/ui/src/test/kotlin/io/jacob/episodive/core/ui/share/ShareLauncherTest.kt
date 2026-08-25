package io.jacob.episodive.core.ui.share

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * 공유 시트로 실제로 나가는 [Intent] 를 고정한다.
 *
 * 문구 조립 규칙은 `:core:model` 의 순수 테스트가 지키므로, 여기서는 그 결과가 손실 없이
 * `ACTION_SEND` 에 실리는지만 본다.
 */
@RunWith(RobolectricTestRunner::class)
class ShareLauncherTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun launcher(onError: (Throwable) -> Unit = {}): ShareLauncher {
        lateinit var launcher: ShareLauncher
        composeTestRule.setContent {
            EpisodiveTheme {
                launcher = rememberShareLauncher(onError = onError)
            }
        }
        return launcher
    }

    @Suppress("DEPRECATION")
    private fun lastSharedIntent(): Intent? =
        shadowOf(composeTestRule.activity).nextStartedActivity
            ?.getParcelableExtra(Intent.EXTRA_INTENT)

    @Test
    fun `Given an episode, When shared, Then an ACTION_SEND chooser carries subject and text`() {
        val episode = episodeTestDataList.first()
        val launcher = launcher()

        composeTestRule.runOnIdle { launcher.share(episode) }

        val shared = requireNotNull(lastSharedIntent())
        assertEquals(Intent.ACTION_SEND, shared.action)
        assertEquals("text/plain", shared.type)
        assertTrue(shared.getStringExtra(Intent.EXTRA_TEXT).orEmpty().contains(episode.title))
        assertTrue(shared.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty().contains(episode.title))
    }

    @Test
    fun `Given a listening position, When shared, Then the timestamp rides along`() {
        val launcher = launcher()

        composeTestRule.runOnIdle {
            launcher.share(episodeTestDataList.first(), positionMs = 83_000L)
        }

        assertTrue(lastSharedIntent()?.getStringExtra(Intent.EXTRA_TEXT).orEmpty().contains("1:23"))
    }

    @Test
    fun `Given a podcast, When shared, Then its own content is used`() {
        val podcast = podcastTestData
        val launcher = launcher()

        composeTestRule.runOnIdle { launcher.share(podcast) }

        val shared = requireNotNull(lastSharedIntent())
        assertEquals(podcast.title, shared.getStringExtra(Intent.EXTRA_SUBJECT))
        assertTrue(shared.getStringExtra(Intent.EXTRA_TEXT).orEmpty().contains(podcast.title))
    }
}
