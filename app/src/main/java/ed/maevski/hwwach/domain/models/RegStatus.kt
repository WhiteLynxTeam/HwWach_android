package ed.maevski.hwwach.domain.models

data class RegStatus(
    val uuid: String? = null,
    val login: String? = null,
    val status: RegStatusEnum? = null,
)