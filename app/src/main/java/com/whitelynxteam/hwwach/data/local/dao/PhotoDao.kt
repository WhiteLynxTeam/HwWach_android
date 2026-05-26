package com.whitelynxteam.hwwach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import com.whitelynxteam.hwwach.data.local.model.PhotoLocalPath
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT COUNT(*) FROM photos LIMIT 1")
    suspend fun hasPhotos(): Int

    @Query("SELECT * FROM photos WHERE clientId = :clientId LIMIT 1")
    suspend fun getPhotoByClientId(clientId: String): PhotoEntity?

    @Query("SELECT * FROM photos WHERE clientId = :clientId")
    fun getPhotosByClientId(clientId: String): Flow<List<PhotoEntity>>

    /** Все фотографии */
    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    /** Фото с фильтрацией по статусу */
    @Query("SELECT * FROM photos WHERE status = :status")
    fun getPhotosByStatus(status: String): Flow<List<PhotoEntity>>

    /** Фото, ожидающие отправки на сервер */
    @Query("SELECT * FROM photos WHERE status = 'PENDING'")
    suspend fun getPendingPhotos(): List<PhotoEntity>

    /** Фото по списку clientId (для целевой синхронизации фото актива) */
    @Query("SELECT * FROM photos WHERE clientId IN (:clientIds)")
    suspend fun getPhotosByClientIds(clientIds: List<String>): List<PhotoEntity>

    /** Фото, загруженные в MinIO, но не подтверждённые бэкендом */
    @Query("SELECT * FROM photos WHERE status = 'UPLOADED'")
    suspend fun getUploadedPhotos(): List<PhotoEntity>

    /** Фото, зависшие на статусе UPLOADING (приложение упало во время загрузки) */
    @Query("SELECT * FROM photos WHERE status = 'UPLOADING'")
    suspend fun getStuckUploadingPhotos(): List<PhotoEntity>

    /** Фото для повторной попытки (FAILED с локальным файлом) */
    @Query("SELECT * FROM photos WHERE status = 'FAILED' AND localFilePath IS NOT NULL")
    suspend fun getRetryablePhotos(): List<PhotoEntity>

    @Query("UPDATE photos SET status = :status WHERE clientId = :clientId")
    suspend fun updatePhotoStatus(clientId: String, status: String)

    @Query("UPDATE photos SET status = :status, serverUuid = :serverUuid WHERE clientId = :clientId")
    suspend fun completeUpload(clientId: String, serverUuid: String, status: String)

    @Query("UPDATE photos SET status = :status, serverUuid = :serverUuid, remoteUrl = :remoteUrl WHERE clientId = :clientId")
    suspend fun completeUploadWithUrl(clientId: String, serverUuid: String, remoteUrl: String, status: String)

    @Query("UPDATE photos SET status = :status, errorMessage = :error WHERE clientId = :clientId")
    suspend fun updateStatusWithError(clientId: String, status: String, error: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE clientId = :clientId")
    suspend fun deletePhotoByClientId(clientId: String)

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()

    /**
     * Получить все существующие локальные пути одним запросом
     * Используется для JOIN-оптимизации при синхронизации
     */
    @Query("SELECT clientId, localFilePath FROM photos WHERE localFilePath IS NOT NULL")
    suspend fun getExistingLocalPaths(): List<PhotoLocalPath>

    /**
     * Удалить SYNCED фото, которых нет в списке (только полностью синхронизированные)
     * Не трогает PENDING/UPLOADING/UPLOADED/FAILED — они могут быть в процессе
     */
    @Query("DELETE FROM photos WHERE status = 'SYNCED' AND clientId NOT IN (:clientIds)")
    suspend fun deleteSyncedPhotosNotInList(clientIds: List<String>)

    /**
     * Умная синхронизация фото с сервера:
     * 1. Удаляет SYNCED фото, которых нет в списке сервера
     * 2. Сохраняет существующие localFilePath через JOIN-логику
     * 3. Обновляет/вставляет данные с сервера
     */
    @Transaction
    suspend fun smartSyncPhotos(serverPhotos: List<PhotoEntity>) {
        // 1. Получаем ID фото с сервера
        val serverIds = serverPhotos.map { it.clientId }

        // 2. Удаляем SYNCED фото, которых нет на сервере (один SQL запрос)
        deleteSyncedPhotosNotInList(serverIds)

        // 3. Получаем все существующие пути одним запросом (JOIN подготовка)
        val existingPaths = getExistingLocalPaths().associate {
            it.clientId to it.localFilePath
        }

        // 4. Мержим: данные сервера + существующие localFilePath
        val mergedPhotos = serverPhotos.map { serverPhoto ->
            serverPhoto.copy(localFilePath = existingPaths[serverPhoto.clientId])
        }

        // 5. INSERT OR REPLACE всех фото одним запросом
        insertPhotos(mergedPhotos)
    }
}
