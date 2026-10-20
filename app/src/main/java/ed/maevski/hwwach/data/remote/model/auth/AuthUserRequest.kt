package ed.maevski.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

data class AuthUserRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String,
)
