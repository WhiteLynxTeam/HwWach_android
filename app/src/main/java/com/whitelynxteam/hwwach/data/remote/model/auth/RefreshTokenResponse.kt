package com.whitelynxteam.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
)
