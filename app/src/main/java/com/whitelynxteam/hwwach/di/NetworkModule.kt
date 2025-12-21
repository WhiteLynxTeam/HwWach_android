package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.BuildConfig
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
        tokensRepository: ITokensRepository  // DI внедряет имплементацию
    ): TokenInterceptor = TokenInterceptor(tokensRepository)

    @Provides
    @Singleton
    @AuthOkHttpClient
    fun provideAuthOkHttpClient(): OkHttpClient = 
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { 
                level = HttpLoggingInterceptor.Level.BODY 
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @ApiOkHttpClient
    fun provideApiOkHttpClient(tokenInterceptor: TokenInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor (tokenInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@AuthOkHttpClient okHttpClient: OkHttpClient): Retrofit = 
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("api")
    fun provideApiRetrofit(@ApiOkHttpClient okHttpClient: OkHttpClient): Retrofit = 
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

//    @Provides
//    @Singleton
//    @Named("auth")
//    fun provideUserApi(@Named("auth") retrofit: Retrofit): UserApi =
//        retrofit.create(UserApi::class.java)
//
//    @Provides
//    @Singleton
//    @Named("api")
//    fun provideSecureUserApi(@Named("api") retrofit: Retrofit): SecureUserApi =
//        retrofit.create(SecureUserApi::class.java)
}
