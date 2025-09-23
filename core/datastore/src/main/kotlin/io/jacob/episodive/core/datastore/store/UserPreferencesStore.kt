package io.jacob.episodive.core.datastore.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.jacob.episodive.core.datastore.model.UserPreferences
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.mapper.toCategories
import io.jacob.episodive.core.model.mapper.toCommaString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    object UserPreferencesKeys {
        val language = stringPreferencesKey("language")
        val categories = stringPreferencesKey("categories")
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { it[UserPreferencesKeys.language] = language }
    }

    suspend fun setCategories(categories: List<Category>) {
        dataStore.edit { it[UserPreferencesKeys.categories] = categories.toCommaString() }
    }

    fun getUserPreferences(): Flow<UserPreferences> =
        dataStore.data.map { preferences ->
            UserPreferences(
                language = preferences[UserPreferencesKeys.language]
                    ?: "en",
                categories = preferences[UserPreferencesKeys.categories]?.toCategories()
                    ?: emptyList(),
            )
        }
}