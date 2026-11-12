package ed.maevski.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import ed.maevski.hwwach.data.local.entity.AssetEntity
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
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>): List<Long>

    @Upsert
    suspend fun upsertAssets(assets: List<AssetEntity>)

    @Query("DELETE FROM assets")
    suspend fun deleteAllAssets()

    @Query("DELETE FROM assets WHERE clientId = :clientId")
    suspend fun deleteAssetByClientId(clientId: String)

    @Query("UPDATE assets SET status = :status WHERE clientId = :clientId")
    suspend fun updateAssetStatus(clientId: String, status: String)

    @Query("SELECT * FROM assets WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): AssetEntity?

    /** Обновление по ключу: в отличие от REPLACE не удаляет строку и не рвёт связи с фото */
    @Update
    suspend fun updateAsset(asset: AssetEntity)

    /** Очередь отправки: новые и зависшие (приложение закрыли посреди отправки) */
    @Query("SELECT * FROM assets WHERE status IN ('PENDING', 'UPLOADING') ORDER BY localCreatedAt")
    suspend fun getUploadQueue(): List<AssetEntity>

    @Query("SELECT serverUuid FROM assets WHERE status = 'SYNCED' AND serverUuid IS NOT NULL")
    suspend fun getSyncedServerUuids(): List<String>

    @Query("DELETE FROM assets WHERE serverUuid IN (:uuids)")
    suspend fun deleteByServerUuids(uuids: List<String>)

    @Query("UPDATE assets SET status = 'FAILED', errorMessage = :error WHERE clientId = :clientId")
    suspend fun markFailed(clientId: String, error: String)

    @Query("UPDATE assets SET status = 'PENDING', errorMessage = NULL WHERE status = 'FAILED'")
    suspend fun requeueFailed(): Int

    @Query("SELECT COUNT(*) FROM assets WHERE status IN ('PENDING', 'UPLOADING')")
    fun observeQueuedCount(): Flow<Int>

    @Query("SELECT * FROM assets WHERE status = 'FAILED' ORDER BY localCreatedAt")
    fun observeFailed(): Flow<List<AssetEntity>>

    @Query("UPDATE assets SET serverUuid = :serverUuid, status = :status, moderationStatus = :moderationStatus, createdAt = :createdAt, updatedAt = :updatedAt, locationKind = :locationKind, locationId = :locationId, locationPath = :locationPath, errorMessage = NULL WHERE clientId = :clientId")
    suspend fun markSynced(
        clientId: String,
        serverUuid: String?,
        status: String,
        moderationStatus: String,
        createdAt: Long?,
        updatedAt: Long?,
        locationKind: String,
        locationId: String?,
        locationPath: String?,
    )

    @Query("UPDATE assets SET serverUuid = :serverUuid, status = :status, moderationStatus = :moderationStatus, createdAt = :createdAt, updatedAt = :updatedAt WHERE clientId = :clientId")
    suspend fun updateAssetSyncInfo(
        clientId: String,
        serverUuid: String?,
        status: String,
        moderationStatus: String,
        createdAt: Long?,
        updatedAt: Long?
    )
}
