package com.whitelynxteam.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

data class AssetsResponseDto(
    @SerializedName("assets") val assets: List<AssetDto>,
    @SerializedName("user_uuid") val userUuid: String
)
