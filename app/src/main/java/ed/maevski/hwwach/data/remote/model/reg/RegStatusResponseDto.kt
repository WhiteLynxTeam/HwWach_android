package ed.maevski.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

data class RegStatusResponseDto(
    @SerializedName("id")
    val uuid: String,
    val login: String,
    val status: RegStatusEnumDto,
)