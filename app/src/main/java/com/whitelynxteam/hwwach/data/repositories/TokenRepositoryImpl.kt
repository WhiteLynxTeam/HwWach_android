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
    private val _tokenCache = MutableStateFlow<String?>(null)
    override val tokenCache: StateFlow<String?> = _tokenCache.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            tokenDataStore.accessToken.collect {
                _tokenCache.value = it
            }
        }
    }

    override val token: Flow<Token?> = tokenDataStore.accessToken.map { tokenString ->
        tokenString?.let {
            Token(
                accessToken = it,
                refreshToken = it,
            )
        }
    }

    override suspend fun saveToken(token: String) {
        tokenDataStore.saveAccessToken(token)
    }

    override suspend fun clearToken() {
        tokenDataStore.clearToken()
    }
}
