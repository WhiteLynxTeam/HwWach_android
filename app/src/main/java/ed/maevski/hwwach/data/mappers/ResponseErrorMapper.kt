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
                val body = runCatching { response.errorBody()?.string() }.getOrNull()
                // Логин есть в нескольких организациях (9.2) — приложение просит номер организации
                if (response.code() == 409 && body?.contains(ORG_NUMBER_REQUIRED) == true) {
                    DomainResult.ServerError(409)
                } else {
                    DomainResult.ValidationError(extractErrorMessage(response, body))
                }
            }
            500 -> DomainResult.ServerError(500)
            else -> DomainResult.NetworkError(response.message().takeIf { it.isNotBlank() } ?: "Unknown error")
        }

    private fun extractErrorMessage(response: Response<*>, body: String?): String {
        return try {
            if (!body.isNullOrBlank()) {
                val json = JSONObject(body)
                when {
                    // Nest: message — текст для человека, error — «Not Found» и т.п.; go отдаёт только error
                    json.has("message") -> {
                        val msgObj = json.opt("message")
                        if (msgObj is org.json.JSONArray) {
                            (0 until msgObj.length()).joinToString("; ") { msgObj.getString(it) }
                        } else {
                            msgObj?.toString() ?: ""
                        }
                    }
                    json.has("error") -> json.optString("error")
                    else -> body
                }
            } else {
                response.message().takeIf { it.isNotBlank() } ?: "Ошибка запроса (${response.code()})"
            }
        } catch (_: Exception) {
            response.message().takeIf { it.isNotBlank() } ?: "Ошибка запроса (${response.code()})"
        }
    }

    private companion object {
        const val ORG_NUMBER_REQUIRED = "ORG_NUMBER_REQUIRED"
    }
}
