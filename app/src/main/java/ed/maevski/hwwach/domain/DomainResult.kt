package ed.maevski.hwwach.domain

// Domain слой — бизнес-ошибки
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()

    data object UnauthorizedError : DomainResult<Nothing>()
    data class ServerError(val code: Int) : DomainResult<Nothing>()
    data class NetworkError(val message: String) : DomainResult<Nothing>()

    data class ValidationError(val message: String) : DomainResult<Nothing>()
}

suspend fun <T, R> DomainResult<T>.mapSuccess(transform: suspend (T) -> R): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> DomainResult.Success(transform(data))

        is DomainResult.UnauthorizedError -> this
        is DomainResult.ServerError -> this
        is DomainResult.NetworkError -> this
        is DomainResult.ValidationError -> this
    }