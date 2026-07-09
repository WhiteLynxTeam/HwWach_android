package ed.maevski.hwwach.domain.exception

open class DomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)