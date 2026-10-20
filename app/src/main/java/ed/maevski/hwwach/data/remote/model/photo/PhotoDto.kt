package ed.maevski.hwwach.data.remote.model.photo

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("uuid")
    val uuid: String,
)
