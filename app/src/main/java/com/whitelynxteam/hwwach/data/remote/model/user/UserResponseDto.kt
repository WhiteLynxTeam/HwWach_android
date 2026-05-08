package com.whitelynxteam.hwwach.data.remote.model.user

data class UserResponseDto(
    val id: String,
    val login: String,
    val phone: String? = null,
    val lastName: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val position: String? = null,
    val role: String,
    val isActive: Boolean? = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)