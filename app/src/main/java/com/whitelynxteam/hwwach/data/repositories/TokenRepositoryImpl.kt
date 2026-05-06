package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.PreferencesDataStore
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.models.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor(
    private val tokenDataStore: PreferencesDataStore
) : ITokensRepository {
    private val _accessTokenCache = MutableStateFlow<String?>(null)
    override val accessTokenCache: StateFlow<String?> = _accessTokenCache.asStateFlow()

    private val _refreshTokenCache = MutableStateFlow<String?>(null)
    override val refreshTokenCache: StateFlow<String?> = _refreshTokenCache.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            tokenDataStore.accessToken.collect {
                _accessTokenCache.value = it
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            tokenDataStore.refreshToken.collect {
                _refreshTokenCache.value = it
            }
        }
    }

    override val token: Flow<Token?> = kotlinx.coroutines.flow.combine(
        tokenDataStore.accessToken,
        tokenDataStore.refreshToken
    ) { access, refresh ->
        if (access != null && refresh != null) {
            Token(accessToken = access, refreshToken = refresh)
        } else null
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenDataStore.saveAccessToken(accessToken)
        tokenDataStore.saveRefreshToken(refreshToken)
    }

    override suspend fun saveAccessToken(accessToken: String) {
        tokenDataStore.saveAccessToken(accessToken)
    }

    override suspend fun clearTokens() {
        tokenDataStore.clearToken()
    }

    override fun hasRefreshToken(): Boolean {
        return _refreshTokenCache.value != null
    }
}
