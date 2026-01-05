package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.auth.AuthUserRequest
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class UserDomainToAuthUserRequestMapper @Inject constructor() {
    fun map(user: User): AuthUserRequest? {
        val username = user.username?.takeIf { it.isNotBlank() } ?: return null
        val password = user.password?.takeIf { it.isNotBlank() } ?: return null
        return AuthUserRequest(username, password)
    }
}