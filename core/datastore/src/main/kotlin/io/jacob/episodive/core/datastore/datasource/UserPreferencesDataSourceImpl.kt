package io.jacob.episodive.core.datastore.datasource

import io.jacob.episodive.core.datastore.model.UserPreferences
import io.jacob.episodive.core.datastore.store.UserPreferencesStore
import io.jacob.episodive.core.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesDataSourceImpl @Inject constructor(
    private val store: UserPreferencesStore
) : UserPreferencesDataSource {
    override suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        store.setFirstLaunch(isFirstLaunch)
    }

    override suspend fun setCategories(categories: List<Category>) {
        store.setCategories(categories)
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return store.getUserPreferences()
    }
}