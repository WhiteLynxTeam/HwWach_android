package ed.maevski.hwwach.data.remote.model.auth

data class ChangeTempPasswordRequest(
    val login: String,
    val oldPassword: String,
    val newPassword: String
)
