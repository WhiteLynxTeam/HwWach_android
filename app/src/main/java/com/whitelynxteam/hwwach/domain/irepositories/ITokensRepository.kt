package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.models.Token
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ITokensRepository {
    val accessTokenCache: StateFlow<String?>
    val refreshTokenCache: StateFlow<String?>
    val token: Flow<Token?>

    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun saveAccessToken(accessToken: String)
    suspend fun clearTokens()

    /** Проверяет, есть ли refresh token для обновления сессии */
    fun hasRefreshToken(): Boolean
}