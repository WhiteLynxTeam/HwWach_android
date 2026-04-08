package com.whitelynxteam.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT COUNT(*) FROM photos LIMIT 1")
    suspend fun hasPhotos(): Int

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE clientId = :clientId")
    fun getPhotosByClientId(clientId: String): Flow<List<PhotoEntity>>

    /** Фото без привязки к устройствам (сиротские) */
    @Query("SELECT * FROM photos WHERE clientId NOT IN (SELECT photoClientId FROM device_photo_cross_ref)")
    fun getOrphanPhotos(): Flow<List<PhotoEntity>>

    /** Фото, ожидающие отправки на сервер */
    @Query("SELECT * FROM photos WHERE status = 'PENDING'")
    suspend fun getPendingPhotos(): List<PhotoEntity>

    @Query("UPDATE photos SET status = :status WHERE clientId = :clientId")
    suspend fun updatePhotoStatus(clientId: String, status: String)

    @Query("UPDATE photos SET status = :status, serverUuid = :serverUuid WHERE clientId = :clientId")
    suspend fun completeUpload(clientId: String, serverUuid: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE clientId = :clientId")
    suspend fun deletePhotoByClientId(clientId: String)

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()
}
