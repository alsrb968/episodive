package io.jacob.episodive.feature.search

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.RecentSearch
import io.jacob.episodive.core.model.SearchResult
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestDataList
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setSearchScreen(
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        onSearch: (String) -> Unit = {},
        recentSearches: List<RecentSearch> = emptyList(),
        searchResult: SearchResult = SearchResult(),
        podcasts: List<Podcast> = podcastTestDataList,
        episodes: List<Episode> = episodeTestDataList,
        onPodcastClick: (Podcast) -> Unit = {},
        onEpisodeClick: (Episode) -> Unit = {},
        onToggleLikedEpisode: (Episode) -> Unit = {},
        onRecentSearchClick: (RecentSearch) -> Unit = {},
        onRemoveRecentSearch: (RecentSearch) -> Unit = {},
        onClearRecentSearches: () -> Unit = {},
        isExpanded: Boolean = false,
        autoFocus: Boolean = false,
        onAutoFocusHandled: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EpisodiveTheme {
                SearchScreen(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    recentSearches = recentSearches,
                    searchResult = searchResult,
                    podcasts = podcasts,
                    episodes = episodes,
                    onPodcastClick = onPodcastClick,
                    onEpisodeClick = onEpisodeClick,
                    onToggleLikedEpisode = onToggleLikedEpisode,
                    onRecentSearchClick = onRecentSearchClick,
                    onRemoveRecentSearch = onRemoveRecentSearch,
                    onClearRecentSearches = onClearRecentSearches,
                    isExpanded = isExpanded,
                    autoFocus = autoFocus,
                    onAutoFocusHandled = onAutoFocusHandled,
                )
            }
        }
    }

    // --- Display tests ---

    @Test
    fun collapsedState_showsTrendingSections() {
        setSearchScreen()

        composeTestRule.onNodeWithText("Global trending feeds").assertIsDisplayed()
    }

    @Test
    fun searchPlaceholderIsDisplayed() {
        setSearchScreen()

        composeTestRule.onNodeWithText("What do you want to listen to?").assertIsDisplayed()
    }

    @Test
    fun emptyQuery_showsPodcastData() {
        setSearchScreen()

        composeTestRule.onNodeWithText(podcastTestDataList.first().title, substring = true)
            .assertExists()
    }

    @Test
    fun expandedState_withSearchResults_showsPodcastsSection() {
        setSearchScreen(
            query = "test",
            searchResult = SearchResult(
                podcasts = podcastTestDataList.take(3),
                episodes = episodeTestDataList,
            ),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Podcasts").assertIsDisplayed()
    }

    @Test
    fun expandedState_withNoResults_showsRecentSearches() {
        setSearchScreen(
            query = "test",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "kotlin", searchedAt = kotlin.time.Clock.System.now()),
                RecentSearch.Query(id = 2, query = "android", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("kotlin").assertIsDisplayed()
    }

    @Test
    fun collapsedState_withEmptyPodcasts_hidesGlobalTrendingSection() {
        setSearchScreen(podcasts = emptyList())

        composeTestRule.onNodeWithText("Global trending feeds").assertDoesNotExist()
    }

    @Test
    fun collapsedState_withEmptyEpisodes_hidesRecentEpisodesSection() {
        setSearchScreen(episodes = emptyList())

        composeTestRule.onNodeWithText("Global recent episodes").assertDoesNotExist()
    }

    @Test
    fun expandedState_withSearchResults_showsEpisodesSection() {
        setSearchScreen(
            query = "test",
            searchResult = SearchResult(
                podcasts = emptyList(),
                episodes = episodeTestDataList,
            ),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Episodes").assertIsDisplayed()
    }

    @Test
    fun collapsedState_showsGlobalRecentEpisodesSection() {
        setSearchScreen(podcasts = emptyList())

        composeTestRule.onNodeWithText("Global recent episodes").assertIsDisplayed()
    }

    // --- New: Search title ---

    @Test
    fun searchTitleIsDisplayed() {
        setSearchScreen()

        composeTestRule.onNodeWithText("Search").assertIsDisplayed()
    }

    // --- New: Recent search types ---

    @Test
    fun expandedState_recentPodcastSearch_showsTitle() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.PodcastSearch(
                    id = 1,
                    podcastId = 100,
                    title = "Kotlin Radio",
                    imageUrl = "",
                    author = "JetBrains",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Kotlin Radio").assertIsDisplayed()
    }

    @Test
    fun expandedState_recentEpisodeSearch_showsTitle() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.EpisodeSearch(
                    id = 1,
                    episodeId = 200,
                    title = "Compose UI Episode",
                    imageUrl = "",
                    feedTitle = "Android Dev",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Compose UI Episode").assertIsDisplayed()
    }

    @Test
    fun expandedState_recentPodcastSearch_showsAuthorSubtitle() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.PodcastSearch(
                    id = 1,
                    podcastId = 100,
                    title = "Kotlin Radio",
                    imageUrl = "",
                    author = "JetBrains",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("JetBrains", substring = true).assertIsDisplayed()
    }

    @Test
    fun expandedState_recentEpisodeSearch_showsFeedTitleSubtitle() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.EpisodeSearch(
                    id = 1,
                    episodeId = 200,
                    title = "Compose UI Episode",
                    imageUrl = "",
                    feedTitle = "Android Dev",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Android Dev", substring = true).assertIsDisplayed()
    }

    // --- New: Recent search with empty author ---

    @Test
    fun expandedState_recentPodcastSearch_emptyAuthor_showsPodcast() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.PodcastSearch(
                    id = 1,
                    podcastId = 100,
                    title = "Mystery Podcast",
                    imageUrl = "",
                    author = "",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Podcast", substring = true).assertExists()
    }

    // --- New: Recent search with empty feed title ---

    @Test
    fun expandedState_recentEpisodeSearch_emptyFeedTitle_showsEpisode() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.EpisodeSearch(
                    id = 1,
                    episodeId = 200,
                    title = "Mystery Episode",
                    imageUrl = "",
                    feedTitle = "",
                    searchedAt = kotlin.time.Clock.System.now(),
                ),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Episode", substring = true).assertExists()
    }

    // --- New: Remove recent search button ---

    @Test
    fun expandedState_removeButtonExists() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "test", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onAllNodesWithContentDescription("Remove Recent Search")
            .onFirst()
            .assertExists()
    }

    @Test
    fun expandedState_removeRecentSearch_callbackInvoked() {
        var removedSearch: RecentSearch? = null
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "test query", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
            onRemoveRecentSearch = { removedSearch = it },
        )

        composeTestRule.onAllNodesWithContentDescription("Remove Recent Search")
            .onFirst()
            .performClick()
        assert(removedSearch != null)
    }

    // --- New: Clear recent searches ---

    @Test
    fun expandedState_clearRecentSearches_buttonExists() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "test", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithContentDescription("Clear recent searches").assertExists()
    }

    @Test
    fun expandedState_clearRecentSearches_callbackInvoked() {
        var clearCalled = false
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "test", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
            onClearRecentSearches = { clearCalled = true },
        )

        composeTestRule.onNodeWithContentDescription("Clear recent searches").performClick()
        assert(clearCalled)
    }

    // --- New: Recent search click callback ---

    @Test
    fun expandedState_clickRecentSearch_callbackInvoked() {
        var clickedSearch: RecentSearch? = null
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "kotlin", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
            onRecentSearchClick = { clickedSearch = it },
        )

        composeTestRule.onNodeWithText("kotlin").performClick()
        assert(clickedSearch != null)
    }

    // --- New: Both podcasts and episodes empty in collapsed ---

    @Test
    fun collapsedState_bothEmpty_noSectionsShown() {
        setSearchScreen(
            podcasts = emptyList(),
            episodes = emptyList(),
        )

        composeTestRule.onNodeWithText("Global trending feeds").assertDoesNotExist()
        composeTestRule.onNodeWithText("Global recent episodes").assertDoesNotExist()
    }

    // --- New: Expanded with both results ---

    @Test
    fun expandedState_withBothPodcastsAndEpisodes_showsPodcastsSection() {
        setSearchScreen(
            query = "test",
            searchResult = SearchResult(
                podcasts = podcastTestDataList.take(3),
                episodes = episodeTestDataList,
            ),
            isExpanded = true,
        )

        composeTestRule.onNodeWithText("Podcasts").assertIsDisplayed()
    }

    // --- New: Search result episode items display ---

    @Test
    fun expandedState_searchResultEpisodes_titlesAreDisplayed() {
        setSearchScreen(
            query = "test",
            searchResult = SearchResult(
                podcasts = emptyList(),
                episodes = episodeTestDataList.take(2),
            ),
            isExpanded = true,
        )

        composeTestRule.onAllNodesWithText(episodeTestDataList[0].title, substring = true)
            .onFirst()
            .assertExists()
    }

    // --- New: Expanded with recent search icon ---

    @Test
    fun expandedState_recentSearchQuery_showsHistoryIcon() {
        setSearchScreen(
            query = "",
            recentSearches = listOf(
                RecentSearch.Query(id = 1, query = "compose", searchedAt = kotlin.time.Clock.System.now()),
            ),
            searchResult = SearchResult(),
            isExpanded = true,
        )

        composeTestRule.onNodeWithContentDescription("Recent Search Icon").assertExists()
    }

    @Test
    fun expandedState_searchResults_noMoreActionIsShown() {
        // 더 보기는 홈 전용이다. 검색 결과에서 누르면 갈 곳이 없으므로 버튼이 뜨면 안 된다.
        // 섹션 컴포넌트의 onMore 기본값을 빈 람다로 되돌리면 여기서 걸린다.
        setSearchScreen(
            query = "compose",
            searchResult = SearchResult(
                podcasts = podcastTestDataList,
                episodes = episodeTestDataList,
            ),
            isExpanded = true,
        )

        composeTestRule
            .onAllNodesWithContentDescription("See all", substring = true)
            .assertCountEquals(0)
    }

    // --- Auto focus (홈의 검색 바로가기로 들어온 경우) ---

    @Test
    fun autoFocus_expandsSearchBar() {
        // 펼침 여부는 접힘 전용 콘텐츠로 판정한다 — 펼쳐지면 트렌딩 섹션 자리에 검색
        // 결과 영역이 들어서므로 그 제목이 사라진다.
        setSearchScreen(autoFocus = true)

        composeTestRule.onNodeWithText("Global trending feeds").assertDoesNotExist()
    }

    @Test
    fun autoFocus_focusesInputField() {
        setSearchScreen(autoFocus = true)

        composeTestRule
            .onNodeWithText("What do you want to listen to?")
            .assertIsFocused()
    }

    @Test
    fun autoFocus_signalIsConsumed() {
        // 소비하지 않으면 검색 탭을 떠났다 하단 바로 다시 들어올 때 키보드가 또 올라온다.
        var handled = false
        setSearchScreen(autoFocus = true, onAutoFocusHandled = { handled = true })

        // 포커스 요청은 한 프레임 뒤로 미뤄져 있다. 프레임을 진행시키지 않으면 아직 아무
        // 일도 일어나지 않은 시점을 검사하게 된다.
        composeTestRule.waitForIdle()

        assertTrue(handled)
    }

    @Test
    fun withoutAutoFocus_searchBarStaysCollapsed() {
        var handled = false
        setSearchScreen(autoFocus = false, onAutoFocusHandled = { handled = true })

        composeTestRule.onNodeWithText("Global trending feeds").assertIsDisplayed()
        assertFalse(handled)
    }
}
