package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.reg.RegUserRequest
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class UserDomainToRegUserRequestMapper @Inject constructor() {
    fun map(user: User): RegUserRequest? {
        val login = user.username?.takeIf { it.isNotBlank() } ?: return null
        val password = user.password?.takeIf { it.isNotBlank() } ?: return null
        val lastName = user.lastName?.takeIf { it.isNotBlank() } ?: return null
        val firstName = user.firstName?.takeIf { it.isNotBlank() } ?: return null
        return RegUserRequest(
            login = login,
            password = password,
            phone = user.phone,
            lastName = lastName,
            firstName = firstName,
            middleName = user.middleName,
            position = user.position,
        )
    }
}