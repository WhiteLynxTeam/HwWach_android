package ed.maevski.hwwach.data.remote.model.photo

import com.google.gson.annotations.SerializedName

data class UploadUrlRequest(
    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("content_type")
    val contentType: String,

    @SerializedName("file_size")
    val fileSize: Long,

    val filename: String,
)
