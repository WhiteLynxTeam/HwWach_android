package ed.maevski.hwwach.data.remote.model.admin

import ed.maevski.hwwach.data.remote.model.reg.RegStatusEnumDto

data class RegistrationRequestDto(
    val id: String,
    val login: String,
    val phone: String? = null,
    val lastName: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val position: String? = null,
    val status: RegStatusEnumDto,
    val createdAt: String? = null
)
