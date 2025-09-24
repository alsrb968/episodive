package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.datastore.datasource.UserPreferencesDataSource
import io.jacob.episodive.core.datastore.mapper.toUserData
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : UserRepository {
    override suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        userPreferencesDataSource.setFirstLaunch(isFirstLaunch)
    }

    override suspend fun setCategories(categories: List<Category>) {
        userPreferencesDataSource.setCategories(categories)
    }

    override fun getUserData(): Flow<UserData> {
        return userPreferencesDataSource.getUserPreferences()
            .map { it.toUserData() }
    }
}