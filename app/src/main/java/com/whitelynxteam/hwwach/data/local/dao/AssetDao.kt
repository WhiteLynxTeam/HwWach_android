package com.whitelynxteam.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whitelynxteam.hwwach.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Query("SELECT COUNT(*) FROM assets LIMIT 1")
    suspend fun hasAssets(): Int

    @Query("SELECT * FROM assets")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE clientId = :clientId")
    fun getAssetByClientId(clientId: String): Flow<AssetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Query("DELETE FROM assets")
    suspend fun deleteAllAssets()

    @Query("DELETE FROM assets WHERE clientId = :clientId")
    suspend fun deleteAssetByClientId(clientId: String)

    @Query("UPDATE assets SET status = :status WHERE clientId = :clientId")
    suspend fun updateAssetStatus(clientId: String, status: String)
}
