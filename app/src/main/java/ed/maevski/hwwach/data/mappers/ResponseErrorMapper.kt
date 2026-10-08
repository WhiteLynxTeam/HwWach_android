package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.domain.DomainResult
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseErrorMapper @Inject constructor() {
    fun <T> map(response: Response<*>): DomainResult<T> =
        when (response.code()) {
            401 -> DomainResult.UnauthorizedError
            412 -> DomainResult.ServerError(412)
            in 400..499 -> {
                val message = extractErrorMessage(response)
                DomainResult.ValidationError(message)
            }
            500 -> DomainResult.ServerError(500)
            else -> DomainResult.NetworkError(response.message().takeIf { it.isNotBlank() } ?: "Unknown error")
        }

    private fun extractErrorMessage(response: Response<*>): String {
        return try {
            val body = response.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                val json = JSONObject(body)
                when {
                    json.has("error") -> json.optString("error")
                    json.has("message") -> {
                        val msgObj = json.opt("message")
                        if (msgObj is org.json.JSONArray) {
                            (0 until msgObj.length()).joinToString("; ") { msgObj.getString(it) }
                        } else {
                            msgObj?.toString() ?: ""
                        }
                    }
                    else -> body
                }
            } else {
                response.message().takeIf { it.isNotBlank() } ?: "Ошибка запроса (${response.code()})"
            }
        } catch (_: Exception) {
            response.message().takeIf { it.isNotBlank() } ?: "Ошибка запроса (${response.code()})"
        }
    }
}
