package ed.maevski.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ed.maevski.hwwach.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY path COLLATE NOCASE")
    fun observeAll(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Query("DELETE FROM locations WHERE synced = 1")
    suspend fun deleteSynced()

    /**
     * Справочник с сервера заменяет прежнюю копию. Заведённые на телефоне (synced = 0) остаются,
     * пока их актив не уйдёт на сервер; если сервер вернул тот же id — запись станет synced
     */
    @Transaction
    suspend fun replaceSynced(locations: List<LocationEntity>) {
        deleteSynced()
        insertAll(locations)
    }

    /** Заведённые на телефоне места, на которые не ссылается ни один не отправленный актив */
    @Query(
        """
        DELETE FROM locations WHERE synced = 0
          AND id NOT IN (SELECT locationId FROM assets WHERE locationId IS NOT NULL AND status != 'SYNCED')
          AND id NOT IN (SELECT parentId FROM locations WHERE parentId IS NOT NULL)
          AND id NOT IN (SELECT locationId FROM photo_groups WHERE locationId IS NOT NULL)
        """
    )
    suspend fun deleteOrphanLocal(): Int

    /** Последние места, куда заводили активы — для выбора одним нажатием */
    @Query(
        """
        SELECT locationId FROM assets WHERE locationId IS NOT NULL
        GROUP BY locationId ORDER BY MAX(localCreatedAt) DESC LIMIT :limit
        """
    )
    fun observeRecentIds(limit: Int): Flow<List<String>>
}
