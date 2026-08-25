package io.jacob.episodive.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.testing.model.episodeTestDataList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

/**
 * 클립 카드의 공유 어포던스 계약.
 *
 * [EpisodeClipItem] 은 여러 화면이 쓰는 공용 컴포넌트라 **`onShare` 를 주지 않은 곳에는
 * 버튼이 뜨지 않아야 한다** — 기본값을 빈 람다로 되돌리면 눌러도 아무 일이 없는 버튼이
 * 생긴다. 섹션 헤더의 `onMore` 와 같은 규약이다([SectionMoreActionTest]).
 */
@RunWith(RobolectricTestRunner::class)
class ShareActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val shareLabel: String
        get() = composeTestRule.activity.getString(R.string.core_ui_share)

    @Test
    fun `Given no onShare, When clip card renders, Then the share button does not exist`() {
        composeTestRule.setContent {
            EpisodiveTheme {
                EpisodeClipItem(
                    episode = episodeTestDataList.first(),
                    isPlaying = false,
                    remaining = 30.seconds,
                    onClick = {},
                    onTogglePlay = {},
                    onToggleLikedEpisode = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shareLabel).assertDoesNotExist()
    }

    @Test
    fun `Given onShare, When the share button is clicked, Then the callback is invoked`() {
        var shared = 0

        composeTestRule.setContent {
            EpisodiveTheme {
                EpisodeClipItem(
                    episode = episodeTestDataList.first(),
                    isPlaying = false,
                    remaining = 30.seconds,
                    onClick = {},
                    onTogglePlay = {},
                    onToggleLikedEpisode = {},
                    onShare = { shared++ },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shareLabel).performClick()

        assertEquals(1, shared)
    }
}
