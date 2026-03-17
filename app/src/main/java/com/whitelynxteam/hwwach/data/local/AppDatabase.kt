package com.whitelynxteam.hwwach.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.whitelynxteam.hwwach.data.local.dao.DeviceDao
import com.whitelynxteam.hwwach.data.local.dao.DevicePhotoCrossRefDao
import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import com.whitelynxteam.hwwach.data.local.entity.DeviceEntity
import com.whitelynxteam.hwwach.data.local.entity.DevicePhotoCrossRef
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity

@Database(
    entities = [PhotoEntity::class, DeviceEntity::class, DevicePhotoCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun deviceDao(): DeviceDao
    abstract fun devicePhotoCrossRefDao(): DevicePhotoCrossRefDao
}
