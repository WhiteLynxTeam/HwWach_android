package com.whitelynxteam.hwwach.data.remote.model.photo

import com.google.gson.annotations.SerializedName

data class UploadUrlResponse(
    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("max_file_size")
    val maxFileSize: Long,

    val method: String,

    @SerializedName("photo_uuid")
    val photoUuid: String,

    @SerializedName("upload_url")
    val uploadUrl: String,
)