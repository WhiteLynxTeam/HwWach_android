package com.whitelynxteam.hwwach.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Qualifier
@Retention(BINARY)
annotation class NoTokenOkHttpClient

@Qualifier
@Retention(BINARY)
annotation class TokenOkHttpClient

@Qualifier
@Retention(BINARY)
annotation class UploadOkHttpClient
