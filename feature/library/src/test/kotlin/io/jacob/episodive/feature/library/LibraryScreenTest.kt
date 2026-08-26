package io.jacob.episodive.feature.library

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LibraryScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setLibraryScreen(
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        onFind: (String) -> Unit = {},
        section: LibrarySection = LibrarySection.All,
        onSectionChange: (LibrarySection) -> Unit = {},
        playedEpisodes: List<Episode> = episodeTestDataList,
        likedEpisodes: List<Episode> = episodeTestDataList,
        savedEpisodes: List<Episode> = episodeTestDataList,
        followedPodcasts: List<Podcast> = podcastTestDataList,
        preferredCategories: List<Category> = listOf(Category.BUSINESS, Category.COMEDY),
        selectableCategories: List<SelectableCategory> = Category.entries.map { SelectableCategory(it, true) },
        playedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>> = flowOf(PagingData.from(episodeTestDataList.map { SeparatedUiModel.Content(it) })),
        likedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>> = flowOf(PagingData.from(episodeTestDataList.map { SeparatedUiModel.Content(it) })),
        savedEpisodesPaging: Flow<PagingData<SeparatedUiModel<Episode>>> = flowOf(PagingData.from(episodeTestDataList.map { SeparatedUiModel.Content(it) })),
        followedPodcastsPaging: Flow<PagingData<SeparatedUiModel<Podcast>>> = flowOf(PagingData.from(podcastTestDataList.map { SeparatedUiModel.Content(it) })),
        onPlayedEpisodeClick: (Episode) -> Unit = {},
        onEpisodeClick: (Episode) -> Unit = {},
        onPodcastClick: (Podcast) -> Unit = {},
        onToggleLikedEpisode: (Episode) -> Unit = {},
        onToggleSavedEpisode: (Episode) -> Unit = {},
        onToggleFollowedPodcast: (Podcast) -> Unit = {},
        onTogglePreferredCategory: (Category) -> Unit = {},
        onOpmlClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EpisodiveTheme {
                LibraryScreen(
                    query = query,
                    onQueryChange = onQueryChange,
                    onFind = onFind,
                    section = section,
                    onSectionChange = onSectionChange,
                    onOpmlClick = onOpmlClick,
                    playedEpisodes = playedEpisodes,
                    likedEpisodes = likedEpisodes,
                    savedEpisodes = savedEpisodes,
                    followedPodcasts = followedPodcasts,
                    preferredCategories = preferredCategories,
                    selectableCategories = selectableCategories,
                    playedEpisodesPaging = playedEpisodesPaging,
                    likedEpisodesPaging = likedEpisodesPaging,
                    savedEpisodesPaging = savedEpisodesPaging,
                    followedPodcastsPaging = followedPodcastsPaging,
                    onPlayedEpisodeClick = onPlayedEpisodeClick,
                    onEpisodeClick = onEpisodeClick,
                    onPodcastClick = onPodcastClick,
                    onToggleLikedEpisode = onToggleLikedEpisode,
                    onToggleSavedEpisode = onToggleSavedEpisode,
                    onToggleFollowedPodcast = onToggleFollowedPodcast,
                    onTogglePreferredCategory = onTogglePreferredCategory,
                )
            }
        }
    }

    // --- Display tests ---

    @Test
    fun allSection_displaysData() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Recently listened episodes").assertIsDisplayed()
    }

    @Test
    fun filterChipsAreDisplayed() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Liked").assertIsDisplayed()
        composeTestRule.onNodeWithText("Followed").assertIsDisplayed()
    }

    @Test
    fun libraryTitleIsDisplayed() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Library").assertIsDisplayed()
    }

    @Test
    fun recentlyListenedSection_displaysData() {
        setLibraryScreen(section = LibrarySection.RecentlyListened)

        composeTestRule.onNodeWithText(episodeTestDataList.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun likedSection_displaysData() {
        setLibraryScreen(section = LibrarySection.Liked)

        composeTestRule.onNodeWithText(episodeTestDataList.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun followedSection_displaysData() {
        setLibraryScreen(section = LibrarySection.Followed)

        composeTestRule.onNodeWithText(podcastTestDataList.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun preferredSection_displaysCategories() {
        setLibraryScreen(section = LibrarySection.Preferred)

        composeTestRule.onNodeWithText("Arts", substring = true).assertExists()
    }

    @Test
    fun allSection_withEmptyData_showsNoResults() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = emptyList(),
            savedEpisodes = emptyList(),
            followedPodcasts = emptyList(),
            preferredCategories = emptyList(),
            selectableCategories = emptyList(),
            playedEpisodesPaging = flowOf(PagingData.from(emptyList())),
            likedEpisodesPaging = flowOf(PagingData.from(emptyList())),
            savedEpisodesPaging = flowOf(PagingData.from(emptyList())),
            followedPodcastsPaging = flowOf(PagingData.from(emptyList())),
        )

        composeTestRule.onNodeWithText("No results found.").assertIsDisplayed()
    }

    @Test
    fun allSection_displaysLikedEpisodesSection() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Liked episodes").assertIsDisplayed()
    }

    @Test
    fun allSection_displaysFollowedPodcastsSection() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Followed podcasts").assertExists()
    }

    @Test
    fun allSection_displaysPreferredCategoriesSection() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = emptyList(),
            savedEpisodes = emptyList(),
            followedPodcasts = emptyList(),
            preferredCategories = listOf(Category.BUSINESS),
            playedEpisodesPaging = flowOf(PagingData.from(emptyList())),
            likedEpisodesPaging = flowOf(PagingData.from(emptyList())),
            savedEpisodesPaging = flowOf(PagingData.from(emptyList())),
            followedPodcastsPaging = flowOf(PagingData.from(emptyList())),
        )

        composeTestRule.onNodeWithText("Preferred categories").assertIsDisplayed()
    }

    @Test
    fun recentlyListenedFilterChipIsDisplayed() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Recently listened").assertIsDisplayed()
    }

    @Test
    fun preferredFilterChipIsDisplayed() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Preferred").assertIsDisplayed()
    }

    @Test
    fun savedSectionTitle_isDisplayed() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Saved").assertIsDisplayed()
    }

    @Test
    fun searchFieldIsDisplayed() {
        setLibraryScreen()

        composeTestRule.onNodeWithContentDescription("search").assertExists()
    }

    @Test
    fun followedPodcastsSection_displaysData() {
        setLibraryScreen()

        composeTestRule.onNodeWithText(podcastTestDataList.first().title, substring = true)
            .assertExists()
    }

    // --- New: Saved section ---

    @Test
    fun savedSection_displaysData() {
        setLibraryScreen(section = LibrarySection.Saved)

        composeTestRule.onNodeWithText(episodeTestDataList.first().title, substring = true)
            .assertExists()
    }

    // --- New: All section conditional rendering ---

    @Test
    fun allSection_onlyPlayedEpisodes_showsRecentlyListened() {
        setLibraryScreen(
            playedEpisodes = episodeTestDataList.take(2),
            likedEpisodes = emptyList(),
            savedEpisodes = emptyList(),
            followedPodcasts = emptyList(),
            preferredCategories = emptyList(),
        )

        composeTestRule.onNodeWithText("Recently listened episodes").assertIsDisplayed()
    }

    @Test
    fun allSection_onlyLikedEpisodes_showsLikedSection() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = episodeTestDataList.take(2),
            savedEpisodes = emptyList(),
            followedPodcasts = emptyList(),
            preferredCategories = emptyList(),
        )

        composeTestRule.onNodeWithText("Liked episodes").assertIsDisplayed()
    }

    @Test
    fun allSection_onlySavedEpisodes_showsSavedSection() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = emptyList(),
            savedEpisodes = episodeTestDataList.take(2),
            followedPodcasts = emptyList(),
            preferredCategories = emptyList(),
        )

        composeTestRule.onNodeWithText("Saved episodes").assertIsDisplayed()
    }

    @Test
    fun allSection_onlyFollowedPodcasts_showsFollowedSection() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = emptyList(),
            savedEpisodes = emptyList(),
            followedPodcasts = podcastTestDataList.take(2),
            preferredCategories = emptyList(),
        )

        composeTestRule.onNodeWithText("Followed podcasts").assertExists()
    }

    @Test
    fun allSection_onlyPreferredCategories_showsPreferredSection() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = emptyList(),
            savedEpisodes = emptyList(),
            followedPodcasts = emptyList(),
            preferredCategories = listOf(Category.ARTS),
        )

        composeTestRule.onNodeWithText("Preferred categories").assertIsDisplayed()
    }

    // --- New: Filter chip section change callback ---

    @Test
    fun clickLikedFilterChip_callbackInvoked() {
        var selectedSection: LibrarySection? = null
        setLibraryScreen(
            onSectionChange = { selectedSection = it },
        )

        composeTestRule.onNodeWithText("Liked").performClick()
        assert(selectedSection == LibrarySection.Liked)
    }

    @Test
    fun clickFollowedFilterChip_callbackInvoked() {
        var selectedSection: LibrarySection? = null
        setLibraryScreen(
            onSectionChange = { selectedSection = it },
        )

        composeTestRule.onNodeWithText("Followed").performClick()
        assert(selectedSection == LibrarySection.Followed)
    }

    @Test
    fun clickSavedFilterChip_callbackInvoked() {
        var selectedSection: LibrarySection? = null
        setLibraryScreen(
            onSectionChange = { selectedSection = it },
        )

        composeTestRule.onNodeWithText("Saved").performClick()
        assert(selectedSection == LibrarySection.Saved)
    }

    @Test
    fun clickPreferredFilterChip_callbackInvoked() {
        var selectedSection: LibrarySection? = null
        setLibraryScreen(
            onSectionChange = { selectedSection = it },
        )

        composeTestRule.onNodeWithText("Preferred").performClick()
        assert(selectedSection == LibrarySection.Preferred)
    }

    @Test
    fun clickRecentlyListenedFilterChip_callbackInvoked() {
        var selectedSection: LibrarySection? = null
        setLibraryScreen(
            onSectionChange = { selectedSection = it },
        )

        composeTestRule.onNodeWithText("Recently listened").performClick()
        assert(selectedSection == LibrarySection.RecentlyListened)
    }

    // --- New: All section shows saved episodes ---

    @Test
    fun allSection_displaysSavedEpisodesSection() {
        setLibraryScreen()

        composeTestRule.onNodeWithText("Saved episodes").assertExists()
    }

    // --- New: Empty played but other data ---

    @Test
    fun allSection_emptyPlayed_noRecentlyListenedSection() {
        setLibraryScreen(
            playedEpisodes = emptyList(),
            likedEpisodes = episodeTestDataList,
            savedEpisodes = episodeTestDataList,
            followedPodcasts = podcastTestDataList,
            preferredCategories = listOf(Category.BUSINESS),
        )

        composeTestRule.onNodeWithText("Recently listened episodes").assertDoesNotExist()
        composeTestRule.onNodeWithText("Liked episodes").assertIsDisplayed()
    }

    @Test
    fun allSection_emptyLiked_noLikedSection() {
        setLibraryScreen(
            playedEpisodes = episodeTestDataList,
            likedEpisodes = emptyList(),
            savedEpisodes = episodeTestDataList,
            followedPodcasts = podcastTestDataList,
            preferredCategories = listOf(Category.BUSINESS),
        )

        composeTestRule.onNodeWithText("Liked episodes").assertDoesNotExist()
    }

    @Test
    fun allSection_emptySaved_noSavedSection() {
        setLibraryScreen(
            playedEpisodes = episodeTestDataList,
            likedEpisodes = episodeTestDataList,
            savedEpisodes = emptyList(),
            followedPodcasts = podcastTestDataList,
            preferredCategories = listOf(Category.BUSINESS),
        )

        composeTestRule.onNodeWithText("Saved episodes").assertDoesNotExist()
    }

    @Test
    fun allSection_emptyFollowed_noFollowedSection() {
        setLibraryScreen(
            playedEpisodes = episodeTestDataList,
            likedEpisodes = episodeTestDataList,
            savedEpisodes = episodeTestDataList,
            followedPodcasts = emptyList(),
            preferredCategories = listOf(Category.BUSINESS),
        )

        composeTestRule.onNodeWithText("Followed podcasts").assertDoesNotExist()
    }

    @Test
    fun allSection_emptyPreferred_noPreferredSection() {
        setLibraryScreen(
            playedEpisodes = episodeTestDataList,
            likedEpisodes = episodeTestDataList,
            savedEpisodes = episodeTestDataList,
            followedPodcasts = podcastTestDataList,
            preferredCategories = emptyList(),
        )

        composeTestRule.onNodeWithText("Preferred categories").assertDoesNotExist()
    }

    @Test
    fun allSection_moreAction_selectsThatSection() {
        // 보관함의 더 보기는 새 화면으로 가지 않는다. 상단 필터를 그 탭으로 옮길 뿐이다.
        var selected: LibrarySection? = null
        setLibraryScreen(onSectionChange = { selected = it })

        composeTestRule
            .onNodeWithContentDescription(seeAll(recentlyListenedTitle))
            .performClick()

        assertEquals(LibrarySection.RecentlyListened, selected)
    }

    @Test
    fun allSection_sectionTitle_selectsThatSection() {
        // 화살표뿐 아니라 제목도 눌려야 한다. 홈과 같은 규칙이다 — 가장 크고 먼저 눈에
        // 들어오는 제목이 죽은 영역이면 사용자는 그걸 몇 번 눌러 본 뒤에야 아이콘을 찾는다.
        var selected: LibrarySection? = null
        setLibraryScreen(onSectionChange = { selected = it })

        composeTestRule.onNodeWithText(recentlyListenedTitle).performClick()

        assertEquals(LibrarySection.RecentlyListened, selected)
    }

    @Test
    fun nonAllSection_back_returnsToAll() {
        // 더 보기로 들어간 탭은 화면이 아니라 필터다. 뒤로가기를 그냥 두면 보관함을 통째로
        // 벗어나, 방금 좁힌 목록이 아니라 탭 전체가 사라진다.
        var selected: LibrarySection? = null
        setLibraryScreen(section = LibrarySection.Liked, onSectionChange = { selected = it })

        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        assertEquals(LibrarySection.All, selected)
    }

    @Test
    fun searchOpen_back_closesSearchInsteadOfLeaving() {
        // 검색창을 여는 상단 액션이 섹션을 All 로 되돌리므로, BackHandler 가 섹션만 보면
        // 검색창이 떠 있는 동안 아예 꺼진다 — 뒤로가기가 보관함 탭을 통째로 벗어난다.
        setLibraryScreen(section = LibrarySection.All)

        composeTestRule.onNodeWithContentDescription("search").performClick()
        composeTestRule.onNodeWithText(findPlaceholder).assertExists()

        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        composeTestRule.onNodeWithText(findPlaceholder).assertDoesNotExist()
        assertFalse("뒤로가기가 화면을 벗어났다", composeTestRule.activity.isFinishing)
    }

    @Test
    fun allSection_back_isLeftToTheSystem() {
        // '모든' 에서는 되돌릴 필터가 없다. 여기서까지 가로채면 보관함을 나갈 수 없다.
        var selected: LibrarySection? = null
        setLibraryScreen(section = LibrarySection.All, onSectionChange = { selected = it })

        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        assertNull(selected)
    }

    /** 영문 기본 리소스가 만들어내는 문자열. */
    private val recentlyListenedTitle = "Recently listened episodes"

    /** 검색창이 떠 있는지 가리는 표식. */
    private val findPlaceholder = "Find your library"

    private fun seeAll(title: String) = "See all $title"
}
