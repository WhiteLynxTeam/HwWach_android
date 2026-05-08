package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.reg.RegResponseDto
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class RegResponseDtoToUserDomainMapper @Inject constructor() {
    fun map(regResponseDto: RegResponseDto): User {
        return User(
            username = regResponseDto.login,
            firstName = regResponseDto.firstName,
            lastName = regResponseDto.lastName,
            middleName = regResponseDto.middleName,
            phone = regResponseDto.phone,
            position = regResponseDto.position,
            createdAt = parseIsoDate(regResponseDto.createdAt) ?: System.currentTimeMillis(),
            updatedAt = parseIsoDate(regResponseDto.updatedAt) ?: System.currentTimeMillis()
        )
    }

    private fun parseIsoDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time
        } catch (e: Exception) {
            try {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format.parse(dateString)?.time
            } catch (e2: Exception) {
                null
            }
        }
    }
}