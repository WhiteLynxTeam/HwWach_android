package ed.maevski.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ed.maevski.hwwach.data.local.entity.PhotoGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoGroupDao {

    @Query("SELECT * FROM photo_groups ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PhotoGroupEntity>>

    @Query("SELECT * FROM photo_groups WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PhotoGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(group: PhotoGroupEntity)

    @Query("DELETE FROM photo_groups WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE photos SET groupId = NULL WHERE groupId = :id")
    suspend fun ungroupPhotos(id: String)

    /** Группа удаляется, снимки остаются — без группы */
    @Transaction
    suspend fun deleteGroup(id: String) {
        ungroupPhotos(id)
        deleteById(id)
    }

    @Query("UPDATE photos SET groupId = :groupId WHERE clientId IN (:photoIds)")
    suspend fun setPhotosGroup(photoIds: List<String>, groupId: String?)

    @Query("UPDATE photos SET note = :note WHERE clientId IN (:photoIds)")
    suspend fun setPhotosNote(photoIds: List<String>, note: String?)

    /** Снимки, уже прикреплённые к активам на телефоне */
    @Query("SELECT DISTINCT photoClientId FROM asset_photo_cross_ref")
    fun observeAttachedPhotoIds(): Flow<List<String>>
}
