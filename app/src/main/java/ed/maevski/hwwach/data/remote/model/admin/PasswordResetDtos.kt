package ed.maevski.hwwach.data.remote.model.admin

import com.google.gson.annotations.SerializedName

/** Заявка «Забыли пароль?» из GET /reset-pass */
data class PasswordResetRequestDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("user")
    val user: PasswordResetUserDto? = null
)

data class PasswordResetUserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("middleName")
    val middleName: String? = null
)

/** Пустой password — сервер сам задаст временный пароль */
data class ApprovePasswordResetDto(
    @SerializedName("password")
    val password: String? = null
)

data class ApprovePasswordResetResponseDto(
    @SerializedName("tempPassword")
    val tempPassword: String? = null,
    @SerializedName("isDefaultPassword")
    val isDefaultPassword: Boolean = false
)
