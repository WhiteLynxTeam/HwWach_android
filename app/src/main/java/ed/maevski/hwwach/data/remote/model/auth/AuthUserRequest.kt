package ed.maevski.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

data class AuthUserRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String,
    // номер организации (9.2): логин уникален только внутри неё; null — вход по почте или однозначному логину
    @SerializedName("organizationNumber")
    val organizationNumber: String? = null,
)
