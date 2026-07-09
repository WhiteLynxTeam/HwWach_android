package ed.maevski.hwwach.data.remote.interceptor

import ed.maevski.hwwach.data.remote.api.UserApi
import ed.maevski.hwwach.data.remote.model.auth.RefreshTokenRequest
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authenticator автоматически обновляет access token при получении 401 ошибки.
 * Вызывается только когда сервер возвращает 401 Unauthorized.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokensRepository: ITokensRepository,
    private val userApi: UserApi
) : Authenticator {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun authenticate(route: Route?, response: Response): Request? {
        // Предотвращаем бесконечный цикл — если уже пытались обновить токен для этого запроса
        if (response.request.header("X-Retry-With-Refresh") != null) {
            scope.launch {
                tokensRepository.clearTokens()
            }
            return null
        }

        // Защита от double refresh:
        // Сверяем токен из запроса с текущим токеном в кэше.
        // Если они отличаются, значит, другой поток уже обновил токен.
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        val currentCachedToken = tokensRepository.accessTokenCache.value

        if (currentCachedToken != null && currentCachedToken != requestToken) {
            // Токен уже обновлен другим запросом. Повторяем запрос с новым токеном.
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentCachedToken")
                .build()
        }

        synchronized(this) {
            // Повторная проверка внутри synchronized блока
            val latestCachedToken = tokensRepository.accessTokenCache.value
            if (latestCachedToken != null && latestCachedToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestCachedToken")
                    .build()
            }

            val refreshToken = tokensRepository.refreshTokenCache.value
                ?: return null // Нет refresh token — не можем обновить

            try {
                // Синхронный сетевой запрос без runBlocking (на фоновом потоке OkHttp)
                val refreshResponse = userApi.refreshToken(
                    RefreshTokenRequest(refreshToken)
                ).execute()

                if (refreshResponse.isSuccessful) {
                    val newAccessToken = refreshResponse.body()?.accessToken
                    if (newAccessToken != null) {
                        // Сохраняем access token асинхронно
                        scope.launch {
                            tokensRepository.saveAccessToken(newAccessToken)
                        }

                        // Повторяем оригинальный запрос с новым access token
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .header("X-Retry-With-Refresh", "true")
                            .build()
                    } else {
                        scope.launch {
                            tokensRepository.clearTokens()
                        }
                        return null
                    }
                } else {
                    // Разлогиниваем только при 401 Unauthorized и 403 Forbidden
                    if (refreshResponse.code() == 401 || refreshResponse.code() == 403) {
                        scope.launch {
                            tokensRepository.clearTokens()
                        }
                    }
                    return null
                }
            } catch (e: Exception) {
                // При ошибках сети или таймаутах (IOException, ConnectException и др.) НЕ сбрасываем токены
                return null
            }
        }
    }
}
