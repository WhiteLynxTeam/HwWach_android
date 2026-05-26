package com.whitelynxteam.hwwach.di

import android.content.Context
import androidx.room.Room
import com.whitelynxteam.hwwach.data.local.AppDatabase
import com.whitelynxteam.hwwach.data.local.RoomTransactionRunner
import com.whitelynxteam.hwwach.data.local.TransactionRunner
import com.whitelynxteam.hwwach.data.local.dao.AssetDao
import com.whitelynxteam.hwwach.data.local.dao.AssetPhotoCrossRefDao
import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
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
