package io.jacob.episodive.core.domain.usecase.user

import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<UserData> {
        return userRepository.getUserData()
    }
}