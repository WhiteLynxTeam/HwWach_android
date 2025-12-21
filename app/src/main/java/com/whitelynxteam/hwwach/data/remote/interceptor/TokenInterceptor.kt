package com.whitelynxteam.hwwach.data.remote.interceptor

import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val tokensRepository: ITokensRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokensRepository.tokenCache.value

        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Token $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}