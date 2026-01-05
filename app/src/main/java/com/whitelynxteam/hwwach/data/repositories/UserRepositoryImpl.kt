package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.mappers.UserDomainToAuthUserRequestMapper
import com.whitelynxteam.hwwach.data.remote.api.UserApi
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.User
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class UserRepositoryImpl @Inject constructor(
    @Named("auth") private val userApi: UserApi,
    private val userDomainToAuthUserRequestMapper: UserDomainToAuthUserRequestMapper,
) : IUserRepository {

    override suspend fun auth(user: User): DomainResult<String> {
        val userAuthRequest = userDomainToAuthUserRequestMapper.map(user)
            ?: return DomainResult.ValidationError(
                when {
                    user.username.isNullOrBlank() -> "Login is empty"
                    user.password.isNullOrBlank() -> "Password is empty"
                    else -> "Invalid credentials"
                }
            )

        val response = userApi.auth(userAuthRequest)

        return when {
            !response.isSuccessful -> mapResponseError(response)
            else -> {
                val token = response.body()?.accessToken ?: return DomainResult.ValidationError("Token not found")
                DomainResult.Success(token)
            }
        }
    }

    private fun mapResponseError(response: Response<*>): DomainResult<String> =
        when (response.code()) {
            in 400..499 -> DomainResult.UnauthorizedError
            500 -> DomainResult.ServerError(500)
            else -> DomainResult.NetworkError(response.message() ?: "Unknown error")
        }
}