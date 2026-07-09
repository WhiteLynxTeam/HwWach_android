package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.local.PreferencesDataStore
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.irepositories.TokenState
import ed.maevski.hwwach.domain.models.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _tokenState = MutableStateFlow<TokenState>(TokenState.Loading)
    override val tokenState: StateFlow<TokenState> = _tokenState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            combine(
                tokenDataStore.accessToken,
                tokenDataStore.refreshToken
            ) { access, refresh ->
                if (access != null && refresh != null) {
                    TokenState.Authenticated(access, refresh)
                } else {
                    TokenState.Unauthenticated
                }
            }.collect { state ->
                _tokenState.value = state
                if (state is TokenState.Authenticated) {
                    _accessTokenCache.value = state.accessToken
                    _refreshTokenCache.value = state.refreshToken
                } else {
                    _accessTokenCache.value = null
                    _refreshTokenCache.value = null
                }
            }
        }
    }

    override val token: Flow<Token?> = tokenState.map { state ->
        if (state is TokenState.Authenticated) {
            Token(accessToken = state.accessToken, refreshToken = state.refreshToken)
        } else {
            null
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        _accessTokenCache.value = accessToken
        _refreshTokenCache.value = refreshToken
        _tokenState.value = TokenState.Authenticated(accessToken, refreshToken)
        tokenDataStore.saveAccessToken(accessToken)
        tokenDataStore.saveRefreshToken(refreshToken)
    }

    override suspend fun saveAccessToken(accessToken: String) {
        _accessTokenCache.value = accessToken
        val currentRefresh = _refreshTokenCache.value
        if (currentRefresh != null) {
            _tokenState.value = TokenState.Authenticated(accessToken, currentRefresh)
        }
        tokenDataStore.saveAccessToken(accessToken)
    }

    override suspend fun clearTokens() {
        _accessTokenCache.value = null
        _refreshTokenCache.value = null
        _tokenState.value = TokenState.Unauthenticated
        tokenDataStore.clearToken()
    }

    override fun hasRefreshToken(): Boolean {
        return _refreshTokenCache.value != null
    }
}
