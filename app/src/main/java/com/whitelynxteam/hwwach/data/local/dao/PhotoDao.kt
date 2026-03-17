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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()

    @Query("DELETE FROM photos WHERE clientId = :clientId")
    suspend fun deletePhotosByClientId(clientId: String)
}
