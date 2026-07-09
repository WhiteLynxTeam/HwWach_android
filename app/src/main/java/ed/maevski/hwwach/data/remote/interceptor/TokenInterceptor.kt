package ed.maevski.hwwach.data.remote.interceptor

import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor добавляет access token к каждому запросу.
 * Не используется если подключен TokenAuthenticator — токен добавляется автоматически
 * при 401, а здесь можно добавить токен к начальному запросу.
 */
class TokenInterceptor @Inject constructor(
    private val tokensRepository: ITokensRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokensRepository.accessTokenCache.value

        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}