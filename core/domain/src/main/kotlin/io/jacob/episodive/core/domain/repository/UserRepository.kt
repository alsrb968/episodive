package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun setFirstLaunch(isFirstLaunch: Boolean)
    suspend fun addCategory(category: Category)
    suspend fun addCategories(categories: List<Category>)
    suspend fun removeCategory(category: Category)
    suspend fun toggleCategory(category: Category): Boolean
    fun getCategories(): Flow<List<Category>>
    fun getUserData(): Flow<UserData>
}