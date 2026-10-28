package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.reg.RegUserRequest
import ed.maevski.hwwach.domain.models.User
import javax.inject.Inject

class UserDomainToRegUserRequestMapper @Inject constructor() {
    fun map(user: User): RegUserRequest? {
        val login = user.username?.takeIf { it.isNotBlank() } ?: return null
        val password = user.password?.takeIf { it.isNotBlank() } ?: return null
        val inviteCode = user.inviteCode?.takeIf { it.isNotBlank() }
        val organizationId = user.organizationId?.takeIf { it.isNotBlank() }
        // без приглашения нужна организация из поиска
        if (inviteCode == null && organizationId == null) return null
        val lastName = user.lastName?.takeIf { it.isNotBlank() }
        val firstName = user.firstName?.takeIf { it.isNotBlank() }
        // по приглашению из карточки ФИО уже на сервере, иначе обязательны
        if (inviteCode == null && (lastName == null || firstName == null)) return null
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
            position = user.position?.takeIf { it.isNotBlank() },
            organizationId = if (inviteCode == null) organizationId else null,
            inviteCode = inviteCode,
        )
    }
}
