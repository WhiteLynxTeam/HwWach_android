package com.whitelynxteam.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

data class RegResponseDto(
    @SerializedName("id")
    val uuid: String,
    val login: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val phone: String,
    val position: String,
    val status: RegStatusEnumDto,
)