package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.admin.RegistrationRequestDto
import ed.maevski.hwwach.domain.models.RegistrationRequest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class RegistrationRequestDtoToDomainMapper @Inject constructor(
    private val regStatusEnumDtoToDomainMapper: RegStatusEnumDtoToDomainMapper
) {
    fun map(dto: RegistrationRequestDto): RegistrationRequest {
        return RegistrationRequest(
            id = dto.id,
            login = dto.login,
            phone = dto.phone,
            lastName = dto.lastName,
            firstName = dto.firstName,
            middleName = dto.middleName,
            position = dto.position,
            status = regStatusEnumDtoToDomainMapper.map(dto.status),
            createdAt = parseIsoDate(dto.createdAt) ?: System.currentTimeMillis()
        )
    }

    private fun parseIsoDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        return listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .firstNotNullOfOrNull { pattern ->
                try {
                    SimpleDateFormat(pattern, Locale.getDefault())
                        .apply { timeZone = TimeZone.getTimeZone("UTC") }
                        .parse(dateString)?.time
                } catch (_: Exception) {
                    null
                }
            }
    }
}
