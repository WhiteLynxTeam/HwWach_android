package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.domain.DomainResult
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseErrorMapper @Inject constructor() {
    fun <T> map(response: Response<*>): DomainResult<T> =
        when (response.code()) {
            412 -> DomainResult.ServerError(412)
            in 400..499 -> DomainResult.UnauthorizedError
            500 -> DomainResult.ServerError(500)
            else -> DomainResult.NetworkError(response.message() ?: "Unknown error")
        }
}
