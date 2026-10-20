package ed.maevski.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

data class ChangeTempPasswordRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("oldPassword")
    val oldPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)
