package com.whitelynxteam.hwwach.data.remote.model.photo

import com.google.gson.annotations.SerializedName

data class CompleteUploadRequest(
    @SerializedName("photo_uuid")
    val photoUuid: String,

    @SerializedName("device_id")
    val deviceId: String? = null,
)
