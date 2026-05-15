package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.domain.irepositories.IAssetRepository
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.irepositories.ISettingsRepository
import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.usecases.AuthApiUseCase
import com.whitelynxteam.hwwach.domain.usecases.CheckRegistrationUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetAssetsUseCase
import com.whitelynxteam.hwwach.domain.usecases.DeletePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetAllPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.SyncPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.ResetStuckUploadsUseCase
import com.whitelynxteam.hwwach.domain.usecases.ResumeUploadedPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.RetrySyncFailedPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.SavePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.SaveLastSyncTimeUseCase
import com.whitelynxteam.hwwach.domain.usecases.SyncPendingPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetLastSyncTimeUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetStartMainScreenDestinationUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetUserInfoUseCase
import com.whitelynxteam.hwwach.domain.usecases.LoginWithProfileUseCase
import com.whitelynxteam.hwwach.domain.usecases.RegApiUseCase
import com.whitelynxteam.hwwach.domain.usecases.CheckAuthTokenUseCase
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
        userProfileRepository: IUserProfileRepository,
        authApiUseCase: AuthApiUseCase
    ): LoginWithProfileUseCase {
        return LoginWithProfileUseCase(userProfileRepository, authApiUseCase)
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
    fun provideSyncPhotosUseCase(
        photoRepository: IPhotoRepository
    ): SyncPhotosUseCase {
        return SyncPhotosUseCase(photoRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllPhotosUseCase(
        photoRepository: IPhotoRepository
    ): GetAllPhotosUseCase {
        return GetAllPhotosUseCase(photoRepository)
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
    fun provideResetStuckUploadsUseCase(
        photoRepository: IPhotoRepository
    ): ResetStuckUploadsUseCase {
        return ResetStuckUploadsUseCase(photoRepository)
    }

    @Provides
    fun provideResumeUploadedPhotosUseCase(
        photoRepository: IPhotoRepository
    ): ResumeUploadedPhotosUseCase {
        return ResumeUploadedPhotosUseCase(photoRepository)
    }

    @Provides
    fun provideRetrySyncFailedPhotosUseCase(
        photoRepository: IPhotoRepository
    ): RetrySyncFailedPhotosUseCase {
        return RetrySyncFailedPhotosUseCase(photoRepository)
    }

    @Provides
    @Singleton
    fun provideGetAssetsUseCase(
        assetRepository: IAssetRepository
    ): GetAssetsUseCase {
        return GetAssetsUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideGetStartMainScreenDestinationUseCase(
        assetRepository: IAssetRepository
    ): GetStartMainScreenDestinationUseCase {
        return GetStartMainScreenDestinationUseCase(assetRepository)
    }

    @Provides
    @Singleton
    fun provideGetLastSyncTimeUseCase(
        settingsRepository: ISettingsRepository
    ): GetLastSyncTimeUseCase {
        return GetLastSyncTimeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSaveLastSyncTimeUseCase(
        settingsRepository: ISettingsRepository
    ): SaveLastSyncTimeUseCase {
        return SaveLastSyncTimeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideCheckAuthTokenUseCase(
        tokensRepository: ITokensRepository
    ): CheckAuthTokenUseCase {
        return CheckAuthTokenUseCase(tokensRepository)
    }
}