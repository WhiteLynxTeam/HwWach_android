package ed.maevski.hwwach.data.remote.model.photo

import com.google.gson.annotations.SerializedName

data class PhotosResponseDto(
    val photos: List<PhotoDto>,

    @SerializedName("user_uuid")
    val userUuid: String,
)