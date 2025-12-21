package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.usecases.AuthApiUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetUserInfoUseCase
import com.whitelynxteam.hwwach.domain.usecases.LoginWithProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAuthApiUseCase(
        userRepository: IUserRepository,
        tokensRepository: ITokensRepository
    ): AuthApiUseCase {
        return AuthApiUseCase(userRepository, tokensRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetUserInfoUseCase(
        userRepository: IUserRepository
    ): GetUserInfoUseCase {
        return GetUserInfoUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideLoginWithProfileUseCase(
        userRepository: IUserRepository,
        tokensRepository: ITokensRepository,
        userProfileRepository: IUserProfileRepository,
        authApiUseCase: AuthApiUseCase,
        getUserInfoUseCase: GetUserInfoUseCase
    ): LoginWithProfileUseCase {
        return LoginWithProfileUseCase(userRepository, tokensRepository, userProfileRepository, authApiUseCase, getUserInfoUseCase)
    }
}