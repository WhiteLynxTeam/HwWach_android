package com.whitelynxteam.hwwach.di

import com.whitelynxteam.hwwach.data.local.storage.FileStorageImpl
import com.whitelynxteam.hwwach.domain.istorage.IFileStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindFileStorage(
        fileStorageImpl: FileStorageImpl
    ): IFileStorage
}
