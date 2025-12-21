package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class LoginWithProfileUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    private val tokensRepository: ITokensRepository,
    private val userProfileRepository: IUserProfileRepository,
    private val authApiUseCase: AuthApiUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) {
    suspend operator fun invoke(user: User): DomainResult<Unit> {
        // Сначала аутентифицируем пользователя
        return when (val authResult = authApiUseCase(user)) {
            is DomainResult.Success<*> -> {
                // Если аутентификация успешна, получаем информацию о пользователе
                when (val userInfoResult = getUserInfoUseCase()) {
                    is DomainResult.Success<*> -> {
                        // Сохраняем профиль пользователя в локальное хранилище
                        userProfileRepository.saveUserProfile(userInfoResult.data as User)
                        DomainResult.Success(Unit)
                    }
                    else -> userInfoResult as DomainResult<Unit>
                }
            }
            else -> {
                // Если аутентификация не удалась, возвращаем ошибку
                authResult as DomainResult<Unit>
            }
        }
    }
}