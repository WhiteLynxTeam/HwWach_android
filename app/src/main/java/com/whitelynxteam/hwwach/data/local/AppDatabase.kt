package com.whitelynxteam.hwwach.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.whitelynxteam.hwwach.data.local.dao.AssetDao
import com.whitelynxteam.hwwach.data.local.dao.AssetPhotoCrossRefDao
import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import com.whitelynxteam.hwwach.data.local.entity.AssetEntity
import com.whitelynxteam.hwwach.data.local.entity.AssetPhotoCrossRef
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity

@Database(
    entities = [PhotoEntity::class, AssetEntity::class, AssetPhotoCrossRef::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun assetDao(): AssetDao
    abstract fun assetPhotoCrossRefDao(): AssetPhotoCrossRefDao
}
