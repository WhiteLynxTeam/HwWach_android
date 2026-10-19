package ed.maevski.hwwach.domain.models

/** Заявка на регистрацию пользователя, ожидающая решения администратора */
data class RegistrationRequest(
    val id: String,
    val login: String,
    val phone: String? = null,
    val lastName: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val position: String? = null,
    val status: RegStatusEnum = RegStatusEnum.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = listOfNotNull(lastName, firstName, middleName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}
