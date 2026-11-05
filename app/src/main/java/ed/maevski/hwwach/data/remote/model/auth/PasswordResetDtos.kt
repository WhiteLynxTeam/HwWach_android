package ed.maevski.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

/** Сброс пароля кодом на почту (8.2), шаг 1: POST /auth/password-reset/send-code */
data class PasswordResetSendCodeRequest(
    @SerializedName("email")
    val email: String,
)

/** Шаг 2: POST /auth/password-reset/confirm */
data class PasswordResetConfirmRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("newPassword")
    val newPassword: String,
)

data class PasswordResetConfirmResponse(
    @SerializedName("login")
    val login: String?,
    @SerializedName("organization")
    val organization: PasswordResetOrgDto?,
)

data class PasswordResetOrgDto(
    @SerializedName("number")
    val number: String?,
    @SerializedName("name")
    val name: String?,
)
