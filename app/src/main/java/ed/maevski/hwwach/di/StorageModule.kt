package ed.maevski.hwwach.di

import ed.maevski.hwwach.data.local.storage.FileStorageImpl
import ed.maevski.hwwach.domain.istorage.IFileStorage
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
