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
            lastName = lastName,
            firstName = firstName,

            // Так для апи сервера пустая строка тоже данные, то поля не проходят валидации
            // на сервере. Делаем поля значением null, чтобы исключить их из json
            // И размер отправляемых данных меньше.
            phone = user.phone?.takeIf { it.isNotBlank() },
            middleName = user.middleName?.takeIf { it.isNotBlank() },
            position = user.position?.takeIf { it.isNotBlank() }
        )
    }
}