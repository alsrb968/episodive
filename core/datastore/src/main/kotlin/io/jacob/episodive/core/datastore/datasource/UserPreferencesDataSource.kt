package io.jacob.episodive.core.datastore.datasource

import io.jacob.episodive.core.datastore.model.UserPreferences
import io.jacob.episodive.core.model.Category
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {
    suspend fun setFirstLaunch(isFirstLaunch: Boolean)
    suspend fun setCategories(categories: List<Category>)
    fun getUserPreferences(): Flow<UserPreferences>
}