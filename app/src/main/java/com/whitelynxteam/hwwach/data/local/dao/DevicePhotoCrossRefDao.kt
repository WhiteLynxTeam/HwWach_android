package com.whitelynxteam.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whitelynxteam.hwwach.data.local.entity.DevicePhotoCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface DevicePhotoCrossRefDao {

    @Query("SELECT * FROM device_photo_cross_ref")
    fun getAllCrossRefs(): Flow<List<DevicePhotoCrossRef>>

    @Query("SELECT * FROM device_photo_cross_ref WHERE deviceClientId = :deviceClientId")
    fun getCrossRefsByDevice(deviceClientId: String): Flow<List<DevicePhotoCrossRef>>

    @Query("SELECT * FROM device_photo_cross_ref WHERE photoClientId = :photoClientId")
    fun getCrossRefsByPhoto(photoClientId: String): Flow<List<DevicePhotoCrossRef>>

    @Query("SELECT * FROM device_photo_cross_ref WHERE deviceClientId = :deviceClientId AND photoClientId = :photoClientId")
    fun getCrossRef(deviceClientId: String, photoClientId: String): Flow<DevicePhotoCrossRef?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: DevicePhotoCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<DevicePhotoCrossRef>)

    @Delete
    suspend fun deleteCrossRef(crossRef: DevicePhotoCrossRef)

    @Query("DELETE FROM device_photo_cross_ref WHERE deviceClientId = :deviceClientId AND photoClientId = :photoClientId")
    suspend fun deleteCrossRef(deviceClientId: String, photoClientId: String)

    @Query("DELETE FROM device_photo_cross_ref WHERE deviceClientId = :deviceClientId")
    suspend fun deleteCrossRefsByDevice(deviceClientId: String)

    @Query("DELETE FROM device_photo_cross_ref WHERE photoClientId = :photoClientId")
    suspend fun deleteCrossRefsByPhoto(photoClientId: String)

    @Query("UPDATE device_photo_cross_ref SET status = :status WHERE deviceClientId = :deviceClientId AND photoClientId = :photoClientId")
    suspend fun updateStatus(deviceClientId: String, photoClientId: String, status: String)
}
