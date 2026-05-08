package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.mapSuccess
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class LoginWithProfileUseCase @Inject constructor(
    private val userProfileRepository: IUserProfileRepository,
    private val authApiUseCase: AuthApiUseCase
) {
    suspend operator fun invoke(login: String, password: String): DomainResult<Unit> {
        return authApiUseCase(
            User(
                username = login,
                password = password,
            )
        ).mapSuccess { user ->
            // Сохраняем профиль пользователя в локальное хранилище
            userProfileRepository.saveUserProfile(user)
        }
    }
}