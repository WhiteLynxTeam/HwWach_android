package ed.maevski.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ed.maevski.hwwach.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("""
        SELECT * FROM categories
        WHERE ((level IN (1, 2) AND status = 'approved') OR (level = 3 AND createdBy = :userUuid AND status != 'rejected'))
          AND (normalizedName LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%')
        ORDER BY level ASC, usageCount DESC, name ASC
        LIMIT :limit
    """)
    fun search(query: String, userUuid: String, limit: Int = 20): Flow<List<CategoryEntity>>

    @Query("""
        SELECT * FROM categories
        WHERE ((level IN (1, 2) AND status = 'approved') OR (level = 3 AND createdBy = :userUuid AND status != 'rejected'))
        ORDER BY level ASC, usageCount DESC, name ASC
        LIMIT :limit
    """)
    fun getTopCategories(userUuid: String, limit: Int = 10): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): CategoryEntity?

    @Query("""
        SELECT * FROM categories
        WHERE normalizedName = :normalizedName
          AND ((level IN (1, 2) AND status = 'approved') OR (level = 3 AND createdBy = :userUuid AND status != 'rejected'))
        ORDER BY level ASC
        LIMIT 1
    """)
    suspend fun findExactMatch(normalizedName: String, userUuid: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE uuid IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}
