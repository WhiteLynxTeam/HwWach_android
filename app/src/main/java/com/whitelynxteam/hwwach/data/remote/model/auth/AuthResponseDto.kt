package com.whitelynxteam.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName
import com.whitelynxteam.hwwach.data.remote.model.user.UserResponseDto

data class AuthResponseDto(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    val user: UserResponseDto,
)