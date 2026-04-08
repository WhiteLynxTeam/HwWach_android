package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.domain.irepositories.IDeviceRepository
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.usecases.AuthApiUseCase
import com.whitelynxteam.hwwach.domain.usecases.CheckRegistrationUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetDevicesUseCase
import com.whitelynxteam.hwwach.domain.usecases.DeletePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetOrphanPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.SavePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.SyncPendingPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetStartMainScreenDestinationUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetUserInfoUseCase
import com.whitelynxteam.hwwach.domain.usecases.LoginWithProfileUseCase
import com.whitelynxteam.hwwach.domain.usecases.RegApiUseCase
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
    fun provideRegApiUseCase(
        userRepository: IUserRepository,
        userProfileRepository: IUserProfileRepository,
    ): RegApiUseCase {
        return RegApiUseCase(userRepository, userProfileRepository)
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

    @Provides
    @Singleton
    fun provideCheckRegistrationUseCase(
        userRepository: IUserRepository
    ): CheckRegistrationUseCase {
        return CheckRegistrationUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetPhotosUseCase(
        photoRepository: IPhotoRepository
    ): GetPhotosUseCase {
        return GetPhotosUseCase(photoRepository)
    }

    @Provides
    @Singleton
    fun provideGetOrphanPhotosUseCase(
        photoRepository: IPhotoRepository
    ): GetOrphanPhotosUseCase {
        return GetOrphanPhotosUseCase(photoRepository)
    }

    @Provides
    fun provideSavePhotoUseCase(
        photoRepository: IPhotoRepository
    ): SavePhotoUseCase {
        return SavePhotoUseCase(photoRepository)
    }

    @Provides
    fun provideDeletePhotoUseCase(
        photoRepository: IPhotoRepository
    ): DeletePhotoUseCase {
        return DeletePhotoUseCase(photoRepository)
    }

    @Provides
    fun provideSyncPendingPhotosUseCase(
        photoRepository: IPhotoRepository
    ): SyncPendingPhotosUseCase {
        return SyncPendingPhotosUseCase(photoRepository)
    }

    @Provides
    @Singleton
    fun provideGetDevicesUseCase(
        deviceRepository: IDeviceRepository
    ): GetDevicesUseCase {
        return GetDevicesUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideGetStartMainScreenDestinationUseCase(
        deviceRepository: IDeviceRepository
    ): GetStartMainScreenDestinationUseCase {
        return GetStartMainScreenDestinationUseCase(deviceRepository)
    }
}