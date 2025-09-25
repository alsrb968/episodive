package io.jacob.episodive.core.domain.usecase.user

import io.jacob.episodive.core.domain.repository.UserRepository
import javax.inject.Inject

class SetFirstLaunchOffUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke() {
        userRepository.setFirstLaunch(false)
    }
}