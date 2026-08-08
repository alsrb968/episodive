package io.jacob.episodive.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.testing.model.channelTestDataList
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 섹션 헤더의 '더 보기' 어포던스 계약을 고정한다.
 *
 * 핵심은 **onMore 를 주지 않은 화면에는 버튼이 뜨지 않는다**는 것이다. 기본값을 빈 람다로
 * 되돌리면 검색·보관함처럼 갈 곳이 없는 화면에도 버튼이 생기고, 눌러도 아무 일이 없다.
 */
@RunWith(RobolectricTestRunner::class)
class SectionMoreActionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val podcastSectionTitle = "Trending"
    private val episodeSectionTitle = "Live"
    private val channelSectionTitle = "Channels"

    /** 섹션 컴포넌트는 [LocalDimensionTheme] 여백을 쓰므로 테마 없이는 렌더되지 않는다. */
    private fun setThemedContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            EpisodiveTheme {
                content()
            }
        }
    }

    @Test
    fun `Given no onMore, When PodcastsSection renders, Then more action does not exist`() {
        setThemedContent {
            PodcastsSection(
                title = podcastSectionTitle,
                podcasts = podcastTestDataList.take(2),
            )
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(podcastSectionTitle))
            .assertDoesNotExist()
    }

    @Test
    fun `Given onMore, When PodcastsSection action clicked, Then callback is invoked`() {
        var clicked = false

        setThemedContent {
            PodcastsSection(
                title = podcastSectionTitle,
                podcasts = podcastTestDataList.take(2),
                onMore = { clicked = true },
            )
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(podcastSectionTitle))
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `Given no onMore, When EpisodesSection renders, Then more action does not exist`() {
        setThemedContent {
            EpisodesSection(
                title = episodeSectionTitle,
                episodes = episodeTestDataList.take(2),
                onEpisodeClick = {},
                onToggleLikedEpisode = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(episodeSectionTitle))
            .assertDoesNotExist()
    }

    @Test
    fun `Given onMore, When EpisodesSection action clicked, Then callback is invoked`() {
        var clicked = false

        setThemedContent {
            EpisodesSection(
                title = episodeSectionTitle,
                episodes = episodeTestDataList.take(2),
                onEpisodeClick = {},
                onToggleLikedEpisode = {},
                onMore = { clicked = true },
            )
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(episodeSectionTitle))
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `Given no onMore, When ChannelSection renders, Then more action does not exist`() {
        setThemedContent {
            ChannelSection(
                title = channelSectionTitle,
                channels = channelTestDataList.take(2),
                onChannelClick = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(channelSectionTitle))
            .assertDoesNotExist()
    }

    @Test
    fun `Given onMore, When ChannelSection action clicked, Then callback is invoked`() {
        var clicked = false

        setThemedContent {
            ChannelSection(
                title = channelSectionTitle,
                channels = channelTestDataList.take(2),
                onChannelClick = {},
                onMore = { clicked = true },
            )
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(channelSectionTitle))
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `Given onMore, When section title clicked, Then callback is invoked`() {
        // 화살표뿐 아니라 제목도 눌려야 한다. 클릭 영역을 아이콘으로 되돌리면 화면에서 가장
        // 크고 먼저 눈에 들어오는 제목이 죽은 영역이 되고, 위의 아이콘 클릭 테스트는 그때도
        // 그대로 통과한다 — 그 회귀를 잡는 것이 이 테스트의 유일한 목적이다.
        var clicked = false

        setThemedContent {
            PodcastsSection(
                title = podcastSectionTitle,
                podcasts = podcastTestDataList.take(2),
                onMore = { clicked = true },
            )
        }

        composeTestRule
            .onNodeWithText(podcastSectionTitle)
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `Given a section with more, When rendered, Then description names that section`() {
        // contentDescription 이 섹션 제목을 담아야 스크린리더 사용자가 어느 섹션의 더 보기인지
        // 구분할 수 있다. 제목을 빼면 화면에 똑같은 버튼이 여덟 개 생긴다.
        // 두 섹션을 함께 그린다. 하나만 그리면 다른 제목의 설명이 없다는 단언은 구현이
        // 어떻든 참이라, 제목을 빼먹은 회귀를 못 잡는다.
        setThemedContent {
            Column {
                PodcastsSection(
                    title = podcastSectionTitle,
                    podcasts = podcastTestDataList.take(1),
                    onMore = {},
                )
                EpisodesSection(
                    title = episodeSectionTitle,
                    episodes = episodeTestDataList.take(1),
                    onEpisodeClick = {},
                    onToggleLikedEpisode = {},
                    onMore = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(seeAll(podcastSectionTitle))
            .assertExists()
        composeTestRule
            .onNodeWithContentDescription(seeAll(episodeSectionTitle))
            .assertExists()
    }

    /** 영문 기본 리소스(`core_ui_section_more_format`)가 만들어내는 문자열. */
    private fun seeAll(title: String) = "See all $title"
}
