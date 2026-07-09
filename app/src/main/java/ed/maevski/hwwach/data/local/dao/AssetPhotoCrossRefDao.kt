package ed.maevski.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ed.maevski.hwwach.data.local.entity.AssetPhotoCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetPhotoCrossRefDao {

    @Query("SELECT * FROM asset_photo_cross_ref")
    fun getAllCrossRefs(): Flow<List<AssetPhotoCrossRef>>

    @Query("SELECT * FROM asset_photo_cross_ref WHERE assetClientId = :assetClientId")
    fun getCrossRefsByAsset(assetClientId: String): Flow<List<AssetPhotoCrossRef>>

    @Query("SELECT * FROM asset_photo_cross_ref WHERE photoClientId = :photoClientId")
    fun getCrossRefsByPhoto(photoClientId: String): Flow<List<AssetPhotoCrossRef>>

    @Query("SELECT * FROM asset_photo_cross_ref WHERE assetClientId = :assetClientId AND photoClientId = :photoClientId")
    fun getCrossRef(assetClientId: String, photoClientId: String): Flow<AssetPhotoCrossRef?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: AssetPhotoCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<AssetPhotoCrossRef>): List<Long>

    @Delete
    suspend fun deleteCrossRef(crossRef: AssetPhotoCrossRef)

    @Query("DELETE FROM asset_photo_cross_ref WHERE assetClientId = :assetClientId AND photoClientId = :photoClientId")
    suspend fun deleteCrossRef(assetClientId: String, photoClientId: String)

    @Query("DELETE FROM asset_photo_cross_ref WHERE assetClientId = :assetClientId")
    suspend fun deleteCrossRefsByAsset(assetClientId: String)

    @Query("DELETE FROM asset_photo_cross_ref WHERE photoClientId = :photoClientId")
    suspend fun deleteCrossRefsByPhoto(photoClientId: String)

    @Query("UPDATE asset_photo_cross_ref SET status = :status WHERE assetClientId = :assetClientId AND photoClientId = :photoClientId")
    suspend fun updateStatus(assetClientId: String, photoClientId: String, status: String)
}
