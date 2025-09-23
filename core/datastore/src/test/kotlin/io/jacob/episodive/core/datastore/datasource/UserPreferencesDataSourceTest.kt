package io.jacob.episodive.core.datastore.datasource

import io.jacob.episodive.core.datastore.store.UserPreferencesStore
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

class UserPreferencesDataSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val store = mockk<UserPreferencesStore>(relaxed = true)

    private val dataSource: UserPreferencesDataSource = UserPreferencesDataSourceImpl(
        store = store,
    )

    @After
    fun teardown() {
        confirmVerified(store)
    }

    @Test
    fun `Given dependencies, When setLanguage, Then call store's method`() =
        runTest {
            // Given
            coEvery { store.setLanguage(any()) } just Runs

            // When
            dataSource.setLanguage("en")

            // Then
            coVerify { store.setLanguage("en") }
        }

    @Test
    fun `Given dependencies, When setCategories, Then call store's method`() =
        runTest {
            // Given
            coEvery { store.setCategories(any()) } just Runs

            // When
            dataSource.setCategories(listOf(Category.CAREERS, Category.SCIENCE))

            // Then
            coVerify { store.setCategories(listOf(Category.CAREERS, Category.SCIENCE)) }
        }

    @Test
    fun `Given dependencies, When getUserPreferences, Then call store's method`() =
        runTest {
            // Given
            coEvery { store.getUserPreferences() } returns mockk()

            // When
            dataSource.getUserPreferences()

            // Then
            coVerify { store.getUserPreferences() }
        }
}