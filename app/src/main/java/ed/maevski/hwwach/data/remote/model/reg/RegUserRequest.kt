package ed.maevski.hwwach.data.remote.model.reg

data class RegUserRequest(
    val login: String,
    val password: String,
    val phone: String? = null,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val position: String? = null,
)
