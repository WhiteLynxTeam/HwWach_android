package ed.maevski.hwwach.domain.models

/** Пароль сменён по коду с почты: логин и организация учётной записи */
data class EmailPasswordResetResult(
    val login: String,
    val organizationNumber: String?,
    val organizationName: String?,
)
