package ed.maevski.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

data class RegStatusResponseDto(
    @SerializedName("id")
    val uuid: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("status")
    val status: RegStatusEnumDto,
)
