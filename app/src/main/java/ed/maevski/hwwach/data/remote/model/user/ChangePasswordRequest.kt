package ed.maevski.hwwach.data.remote.model.user

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
