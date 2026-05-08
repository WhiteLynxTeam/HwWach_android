package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.user.UserResponseDto
import com.whitelynxteam.hwwach.domain.models.User
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class UserResponseDtoToUserDomainMapper @Inject constructor() {
    fun map(userResponseDto: UserResponseDto): User {
        return User(
            username = userResponseDto.login,
            firstName = userResponseDto.firstName,
            lastName = userResponseDto.lastName,
            middleName = userResponseDto.middleName,
            phone = userResponseDto.phone,
            position = userResponseDto.position,
            role = userResponseDto.role,
            isActive = userResponseDto.isActive ?: true,
            createdAt = parseIsoDate(userResponseDto.createdAt) ?: System.currentTimeMillis(),
            updatedAt = parseIsoDate(userResponseDto.updatedAt) ?: System.currentTimeMillis()
        )
    }

    private fun parseIsoDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time
        } catch (e: Exception) {
            try {
                // Fallback for dates without milliseconds
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(dateString)?.time
            } catch (e2: Exception) {
                null
            }
        }
    }
}
