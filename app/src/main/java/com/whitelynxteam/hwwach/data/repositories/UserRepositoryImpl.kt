package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.User
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class UserRepositoryImpl @Inject constructor(
) : IUserRepository {

    /*[red]  Исправить на возврат Токена модели домен слоя*/
    override suspend fun auth(user: User): DomainResult<String> {
        return DomainResult.Success("mock")
    }

    override suspend fun getUserInfo(): DomainResult<User> {
        return DomainResult.Success(User())
    }

    private fun mapResponseError(response: Response<*>): DomainResult<String> =
        when (response.code()) {
            in 400..499 -> DomainResult.UnauthorizedError
            500 -> DomainResult.ServerError(500)
            else -> DomainResult.NetworkError(response.message() ?: "Unknown error")
        }
}