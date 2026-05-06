package com.whitelynxteam.hwwach.data.remote.interceptor

import com.whitelynxteam.hwwach.data.remote.api.UserApi
import com.whitelynxteam.hwwach.data.remote.model.auth.RefreshTokenRequest
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import kotlinx.coroutines.runBlocking
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

    override fun authenticate(route: Route?, response: Response): Request? {
        // Предотвращаем бесконечный цикл — если уже пытались обновить токен для этого запроса
        if (response.request.header("X-Retry-With-Refresh") != null) {
            // Очищаем токены, так как refresh не сработал
            runBlocking {
                tokensRepository.clearTokens()
            }
            return null
        }

        val refreshToken = tokensRepository.refreshTokenCache.value
            ?: return null // Нет refresh token — не можем обновить

        return runBlocking {
            try {
                val refreshResponse = userApi.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )

                if (refreshResponse.isSuccessful) {
                    val newAccessToken = refreshResponse.body()?.accessToken
                    if (newAccessToken != null) {
                        // Сохраняем только новый access token (refresh token не меняется)
                        tokensRepository.saveAccessToken(newAccessToken)

                        // Повторяем оригинальный запрос с новым access token
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .header("X-Retry-With-Refresh", "true")
                            .build()
                    } else {
                        tokensRepository.clearTokens()
                        null
                    }
                } else {
                    // Refresh token невалиден — очищаем токены
                    tokensRepository.clearTokens()
                    null
                }
            } catch (e: Exception) {
                // Ошибка сети при обновлении — очищаем токены
                tokensRepository.clearTokens()
                null
            }
        }
    }
}
