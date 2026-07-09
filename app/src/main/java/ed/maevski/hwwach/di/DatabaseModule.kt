package ed.maevski.hwwach.di

import android.content.Context
import androidx.room.Room
import ed.maevski.hwwach.data.local.AppDatabase
import ed.maevski.hwwach.data.local.RoomTransactionRunner
import ed.maevski.hwwach.data.local.TransactionRunner
import ed.maevski.hwwach.data.local.dao.AssetDao
import ed.maevski.hwwach.data.local.dao.AssetPhotoCrossRefDao
import ed.maevski.hwwach.data.local.dao.PhotoDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRunner(
        impl: RoomTransactionRunner
    ): TransactionRunner

    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "hwwach_db"
            )
                .build()
        }

        @Provides
        @Singleton
        fun providePhotoDao(database: AppDatabase): PhotoDao {
            return database.photoDao()
        }

        @Provides
        @Singleton
        fun provideAssetDao(database: AppDatabase): AssetDao {
            return database.assetDao()
        }

        @Provides
        @Singleton
        fun provideAssetPhotoCrossRefDao(database: AppDatabase): AssetPhotoCrossRefDao {
            return database.assetPhotoCrossRefDao()
        }
    }
}
