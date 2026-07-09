package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.models.Token
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed class TokenState {
    data object Loading : TokenState()
    data class Authenticated(val accessToken: String, val refreshToken: String) : TokenState()
    data object Unauthenticated : TokenState()
}

interface ITokensRepository {
    val accessTokenCache: StateFlow<String?>
    val refreshTokenCache: StateFlow<String?>
    val tokenState: StateFlow<TokenState>
    val token: Flow<Token?>

    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun saveAccessToken(accessToken: String)
    suspend fun clearTokens()

    /** Проверяет, есть ли refresh token для обновления сессии */
    fun hasRefreshToken(): Boolean
}