package io.jacob.episodive.core.datastore.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UserPreferencesStoreTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userPreferencesStore: UserPreferencesStore
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Before
    fun setup() {
        val testFile = File(tmpFolder.root, "test_preferences.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        userPreferencesStore = UserPreferencesStore(dataStore)
    }

    @Test
    fun getUserPreferences_withDefaultValues_returnsDefaults() = runTest {
        val result = userPreferencesStore.getUserPreferences().first()

        assertEquals("en", result.language)
        assertTrue(result.categories.isEmpty())
    }

    @Test
    fun setLanguage_updatesLanguagePreference() = runTest {
        userPreferencesStore.setLanguage("ko")

        val result = userPreferencesStore.getUserPreferences().first()
        assertEquals("ko", result.language)
    }

    @Test
    fun setCategories_withSingleCategory_storesCorrectly() = runTest {
        val categories = listOf(Category.TECHNOLOGY)
        userPreferencesStore.setCategories(categories)

        val result = userPreferencesStore.getUserPreferences().first()
        assertEquals(1, result.categories.size)
        assertEquals(Category.TECHNOLOGY, result.categories.first())
    }

    @Test
    fun setCategories_withMultipleCategories_storesCorrectly() = runTest {
        val categories = listOf(Category.TECHNOLOGY, Category.SCIENCE, Category.HEALTH)
        userPreferencesStore.setCategories(categories)

        val result = userPreferencesStore.getUserPreferences().first()
        assertEquals(3, result.categories.size)
        assertTrue(result.categories.contains(Category.TECHNOLOGY))
        assertTrue(result.categories.contains(Category.SCIENCE))
        assertTrue(result.categories.contains(Category.HEALTH))
    }
}