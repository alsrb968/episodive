package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun setLanguage(language: String)
    suspend fun setCategories(categories: List<Category>)
    fun getUserData(): Flow<UserData>
}