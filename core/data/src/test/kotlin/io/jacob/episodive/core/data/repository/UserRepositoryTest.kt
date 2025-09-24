package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.datastore.datasource.UserPreferencesDataSource
import io.jacob.episodive.core.domain.repository.UserRepository
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

class UserRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userPreferencesDataSource = mockk<UserPreferencesDataSource>(relaxed = true)

    private val repository: UserRepository = UserRepositoryImpl(
        userPreferencesDataSource = userPreferencesDataSource
    )

    @After
    fun teardown() {
        confirmVerified(userPreferencesDataSource)
    }

    @Test
    fun `Given dependencies, When setFirstLaunch, Then call data source's method`() =
        runTest {
            // Given
            coEvery { userPreferencesDataSource.setFirstLaunch(any()) } just Runs

            // When
            repository.setFirstLaunch(false)

            // Then
            coVerify { userPreferencesDataSource.setFirstLaunch(false) }
        }

    @Test
    fun `Given dependencies, When setCategories, Then call data source's method`() =
        runTest {
            // Given
            coEvery { userPreferencesDataSource.setCategories(any()) } just Runs

            // When
            repository.setCategories(emptyList())

            // Then
            coVerify { userPreferencesDataSource.setCategories(emptyList()) }
        }

    @Test
    fun `Given dependencies, When getUserData, Then call data source's method`() =
        runTest {
            // Given
            coEvery { userPreferencesDataSource.getUserPreferences() } returns mockk()

            // When
            repository.getUserData()

            // Then
            coVerify { userPreferencesDataSource.getUserPreferences() }
        }
}