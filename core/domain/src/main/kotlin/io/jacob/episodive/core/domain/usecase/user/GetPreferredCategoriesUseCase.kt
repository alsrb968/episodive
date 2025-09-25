package io.jacob.episodive.core.domain.usecase.user

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPreferredCategoriesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return userRepository.getCategories()
    }
}