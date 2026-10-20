package ed.maevski.hwwach.data.remote.model.user

import com.google.gson.annotations.SerializedName

data class UserResponseDto(
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
    @SerializedName("role")
    val role: String,
    @SerializedName("isActive")
    val isActive: Boolean? = true,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
