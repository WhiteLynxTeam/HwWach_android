package ed.maevski.hwwach.di

import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.irepositories.ISettingsRepository
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import ed.maevski.hwwach.domain.usecases.asset.AddAssetUseCase
import ed.maevski.hwwach.domain.usecases.asset.GetAssetsUseCase
import ed.maevski.hwwach.domain.usecases.asset.GetStartMainScreenDestinationUseCase
import ed.maevski.hwwach.domain.usecases.photo.DeletePhotoUseCase
import ed.maevski.hwwach.domain.usecases.photo.GetAllPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.ResetStuckUploadsUseCase
import ed.maevski.hwwach.domain.usecases.photo.ResumeUploadedPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.RetrySyncFailedPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.SavePhotoUseCase
import ed.maevski.hwwach.domain.usecases.photo.SyncPendingPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.SyncPhotosUseCase
import ed.maevski.hwwach.domain.usecases.settings.GetLastSyncTimeUseCase
import ed.maevski.hwwach.domain.usecases.settings.SaveLastSyncTimeUseCase
import ed.maevski.hwwach.domain.usecases.user.AuthApiUseCase
import ed.maevski.hwwach.domain.usecases.user.ChangePasswordUseCase
import ed.maevski.hwwach.domain.usecases.user.ChangeTempPasswordUseCase
import ed.maevski.hwwach.domain.usecases.user.CheckAuthTokenUseCase
import ed.maevski.hwwach.domain.usecases.user.CheckRegistrationUseCase
import ed.maevski.hwwach.domain.usecases.user.GetUserInfoUseCase
import ed.maevski.hwwach.domain.usecases.user.LoginWithProfileUseCase
import ed.maevski.hwwach.domain.usecases.user.RegApiUseCase
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
    fun provideAddAssetUseCase(
        assetRepository: IAssetRepository
    ): AddAssetUseCase {
        return AddAssetUseCase(assetRepository)
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

    @Provides
    @Singleton
    fun provideChangeTempPasswordUseCase(
        userRepository: IUserRepository
    ): ChangeTempPasswordUseCase {
        return ChangeTempPasswordUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideChangePasswordUseCase(
        userRepository: IUserRepository
    ): ChangePasswordUseCase {
        return ChangePasswordUseCase(userRepository)
    }
}