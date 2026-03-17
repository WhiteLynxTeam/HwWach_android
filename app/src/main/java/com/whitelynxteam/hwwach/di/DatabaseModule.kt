package com.whitelynxteam.hwwach.di

import android.content.Context
import androidx.room.Room
import com.whitelynxteam.hwwach.data.local.AppDatabase
import com.whitelynxteam.hwwach.data.local.dao.DeviceDao
import com.whitelynxteam.hwwach.data.local.dao.DevicePhotoCrossRefDao
import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hwwach_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePhotoDao(database: AppDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(database: AppDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    @Singleton
    fun provideDevicePhotoCrossRefDao(database: AppDatabase): DevicePhotoCrossRefDao {
        return database.devicePhotoCrossRefDao()
    }
}
