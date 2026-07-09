package ed.maevski.hwwach.di

import ed.maevski.hwwach.data.repositories.AssetRepositoryImpl
import ed.maevski.hwwach.data.repositories.PhotoRepositoryImpl
import ed.maevski.hwwach.data.repositories.SettingsRepositoryImpl
import ed.maevski.hwwach.data.repositories.TokenRepositoryImpl
import ed.maevski.hwwach.data.repositories.UserProfileRepositoryImpl
import ed.maevski.hwwach.data.repositories.UserRepositoryImpl
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.irepositories.ISettingsRepository
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import ed.maevski.hwwach.domain.irepositories.IUserRepository
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

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): IPhotoRepository

    @Binds
    @Singleton
    abstract fun bindAssetRepository(
        assetRepositoryImpl: AssetRepositoryImpl
    ): IAssetRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): ISettingsRepository
}