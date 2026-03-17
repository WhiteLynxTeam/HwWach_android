package com.whitelynxteam.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whitelynxteam.hwwach.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT COUNT(*) FROM devices LIMIT 1")
    suspend fun hasDevices(): Int

    @Query("SELECT * FROM devices")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE clientId = :clientId")
    fun getDeviceByClientId(clientId: String): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()

    @Query("DELETE FROM devices WHERE clientId = :clientId")
    suspend fun deleteDeviceByClientId(clientId: String)
}
