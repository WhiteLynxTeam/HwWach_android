package ed.maevski.hwwach.data.remote.model.user

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)
