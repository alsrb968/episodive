package io.jacob.episodive.core.domain.usecase.user

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Category
import javax.inject.Inject

class ToggleCategoryUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(category: Category): Boolean {
        return userRepository.toggleCategory(category)
    }
}