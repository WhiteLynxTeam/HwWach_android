package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.data.repositories.TokenRepositoryImpl
import com.whitelynxteam.hwwach.data.repositories.UserProfileRepositoryImpl
import com.whitelynxteam.hwwach.data.repositories.UserRepositoryImpl
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): IUserRepository

    @Binds
    @Singleton
    abstract fun bindTokenRepository(
        tokenRepositoryImpl: TokenRepositoryImpl
    ): ITokensRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): IUserProfileRepository
}