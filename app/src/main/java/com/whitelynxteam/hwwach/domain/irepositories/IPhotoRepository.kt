package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow

interface IPhotoRepository {
    /**
     * Синхронизация фото с сервером.
     * Не возвращает данные — используйте getAllPhotosFlow() для получения фото из SSOT (БД).
     */
    suspend fun syncPhotos(): DomainResult<Unit>

    fun getPhotosFlow(clientId: String): Flow<List<Photo>>
    fun getAllPhotosFlow(): Flow<List<Photo>>

    fun getPhotosByStatusFlow(status: String): Flow<List<Photo>>
    suspend fun savePhoto(photo: Photo)
    suspend fun deletePhoto(clientId: String)
    suspend fun getPendingPhotos(): List<Photo>
    suspend fun syncPendingPhotos()
    /** Завершить подтверждение для фото со статусом UPLOADED (отправить complete-upload на бэк) */
    suspend fun resumeUploadedPhotos()
    /** Сбросить зависшие UPLOADING → FAILED для повторной отправки */
    suspend fun resetStuckUploads()
    /** Повторить отправку FAILED фото (у которых есть локальный файл) */
    suspend fun retrySyncFailedPhotos()
}
