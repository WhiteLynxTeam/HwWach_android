package com.whitelynxteam.hwwach.data.remote.model.user

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
