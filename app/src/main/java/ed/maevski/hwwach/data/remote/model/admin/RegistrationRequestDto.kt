package ed.maevski.hwwach.data.remote.model.admin

import com.google.gson.annotations.SerializedName
import ed.maevski.hwwach.data.remote.model.reg.RegStatusEnumDto

data class RegistrationRequestDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("middleName")
    val middleName: String? = null,
    @SerializedName("position")
    val position: String? = null,
    @SerializedName("status")
    val status: RegStatusEnumDto,
    @SerializedName("createdAt")
    val createdAt: String? = null
)
