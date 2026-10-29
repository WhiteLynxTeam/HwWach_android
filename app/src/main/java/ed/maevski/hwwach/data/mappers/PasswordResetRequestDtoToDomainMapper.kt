package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.admin.PasswordResetRequestDto
import ed.maevski.hwwach.domain.models.PasswordResetRequest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class PasswordResetRequestDtoToDomainMapper @Inject constructor() {
    fun map(dto: PasswordResetRequestDto): PasswordResetRequest {
        return PasswordResetRequest(
            id = dto.id,
            login = dto.user?.login.orEmpty(),
            lastName = dto.user?.lastName,
            firstName = dto.user?.firstName,
            middleName = dto.user?.middleName,
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
