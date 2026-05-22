package com.whitelynxteam.hwwach.domain.usecases.user

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.mapSuccess
import com.whitelynxteam.hwwach.domain.models.User

class AuthApiUseCase(
    private val userRepository: IUserRepository,
    private val tokensRepository: ITokensRepository,
) {
    // Почему возвращаем DomainResult<Unit>, а не Boolean. Ведь данные о токена мы выдрали из
    // success и во вьюмодель мы данные при успешном результате не отправляем.
    // Но зато мы отправляем результат ошибки с данными - кодом или сообщением,
    // чтобы обработать и выдать сообщение согласно ошибки.

    suspend operator fun invoke(user: User): DomainResult<User> {
        return userRepository.auth(user).mapSuccess { (token, mappedUser) ->
            tokensRepository.saveTokens(
                accessToken = token.accessToken,
                refreshToken = token.refreshToken
            )
            mappedUser
        }
    }
}