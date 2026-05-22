package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.BuildConfig
import com.whitelynxteam.hwwach.data.remote.api.PhotosApi
import com.whitelynxteam.hwwach.data.remote.api.UserApi
import com.whitelynxteam.hwwach.data.remote.api.AssetApi
import com.whitelynxteam.hwwach.data.remote.api.UserTokensApi
import com.whitelynxteam.hwwach.data.remote.interceptor.TokenAuthenticator
import com.whitelynxteam.hwwach.data.remote.interceptor.TokenInterceptor
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenInterceptor(
        tokensRepository: ITokensRepository
    ): TokenInterceptor = TokenInterceptor(tokensRepository)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokensRepository: ITokensRepository,
        @Named("auth") userApi: UserApi
    ): TokenAuthenticator = TokenAuthenticator(tokensRepository, userApi)

    // --- OkHttp Clients ---

    @Provides
    @Singleton
    @UploadOkHttpClient
    fun provideUploadOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    /** Клиент без токена — для запросов, где авторизация не нужна (login, register) */
    @Provides
    @Singleton
    @NoTokenOkHttpClient
    fun provideNoTokenOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    /** Клиент с токеном — для запросов, где нужна авторизация */
    @Provides
    @Singleton
    @TokenOkHttpClient
    fun provideTokenOkHttpClient(
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    // --- Retrofit instances ---

    /** TS-сервис, порт :3033, без токена — регистрация, логин */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@NoTokenOkHttpClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_AUTH_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /** TS-сервис, порт :3033, с токеном — профиль, logout и т.д. */
    @Provides
    @Singleton
    @Named("user_token")
    fun provideUserTokensRetrofit(@TokenOkHttpClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_AUTH_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /** Go-сервис, порт :8080, с токеном — фото, устройства, карточки */
    @Provides
    @Singleton
    @Named("api")
    fun provideApiRetrofit(@TokenOkHttpClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_MAIN_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // --- API interfaces ---

    @Provides
    @Singleton
    @Named("auth")
    fun provideUserApi(@Named("auth") retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    @Named("user_token")
    fun provideUserTokensApi(@Named("user_token") retrofit: Retrofit): UserTokensApi =
        retrofit.create(UserTokensApi::class.java)

    @Provides
    @Singleton
    @Named("api")
    fun providePhotosApi(@Named("api") retrofit: Retrofit): PhotosApi =
        retrofit.create(PhotosApi::class.java)

    @Provides
    @Singleton
    @Named("api")
    fun provideAssetApi(@Named("api") retrofit: Retrofit): AssetApi =
        retrofit.create(AssetApi::class.java)
}
