package ed.maevski.hwwach.domain.models

/** Заявка пользователя на сброс пароля, ожидающая решения администратора */
data class PasswordResetRequest(
    val id: String,
    val login: String,
    val lastName: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = listOfNotNull(lastName, firstName, middleName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}

/**
 * Итог сброса: временный пароль, который админ передаёт пользователю.
 * isDefault — на сервере задан общий пароль по умолчанию, сам пароль в ответ не приходит
 */
data class PasswordResetResult(
    val tempPassword: String?,
    val isDefault: Boolean
)
