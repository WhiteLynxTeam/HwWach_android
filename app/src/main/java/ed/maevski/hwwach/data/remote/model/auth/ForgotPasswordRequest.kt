package ed.maevski.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

/** «Забыли пароль?»: заявка админу организации, он задаёт временный пароль */
data class ForgotPasswordRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("organizationNumber")
    val organizationNumber: String? = null,
)
