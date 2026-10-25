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
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("position")
    val position: String? = null,
    // код системной роли: admin / asset_manager / reviewer / member / observer
    @SerializedName("role")
    val role: String,
    @SerializedName("roleName")
    val roleName: String? = null,
    // права — по ним строим меню, а не по роли
    @SerializedName("perms")
    val perms: List<String>? = null,
    @SerializedName("personId")
    val personId: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("organizationName")
    val organizationName: String? = null,
    // материально ответственное лицо
    @SerializedName("isMol")
    val isMol: Boolean? = false,
    // просил признак МОЛ, ждёт админа
    @SerializedName("molRequested")
    val molRequested: Boolean? = false,
    @SerializedName("isActive")
    val isActive: Boolean? = true,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
