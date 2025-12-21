package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.User

class AuthApiUseCase(
    private val userRepository: IUserRepository,
    private val tokensRepository: ITokensRepository,
) {
    suspend operator fun invoke(user: User): DomainResult<Unit> {
        return when (val result = userRepository.auth(user)) {
            is DomainResult.Success -> {
                tokensRepository.saveToken(result.data)
                DomainResult.Success(Unit)
            }

            else -> result
        } as DomainResult<Unit>
    }
}