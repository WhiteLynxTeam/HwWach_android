package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow

interface IPhotoRepository {
    /**
     * Синхронизация фото с сервером.
     * Не возвращает данные — используйте getAllPhotosFlow() для получения фото из SSOT (БД).
     */
    suspend fun syncPhotos(): DomainResult<Unit>

    fun getPhotosFlow(clientId: String): Flow<List<Photo>>
    suspend fun getPhotoByClientId(clientId: String): Photo?
    fun getAllPhotosFlow(): Flow<List<Photo>>

    fun getPhotosByStatusFlow(status: String): Flow<List<Photo>>
    suspend fun savePhoto(photo: Photo)
    suspend fun deletePhoto(clientId: String)
    suspend fun getPendingPhotos(): List<Photo>
    suspend fun syncPendingPhotos()
    /**
     * Загружает на сервер только фото с указанными clientId.
     * @throws ed.maevski.hwwach.domain.exception.PhotoSyncException если хотя бы одно фото не удалось загрузить/подтвердить.
     */
    suspend fun syncPhotosByClientIds(clientIds: List<String>)
    /** Завершить подтверждение для фото со статусом UPLOADED (отправить complete-upload на бэк) */
    suspend fun resumeUploadedPhotos()
    /** Сбросить зависшие UPLOADING → FAILED для повторной отправки */
    suspend fun resetStuckUploads()
    /** Повторить отправку FAILED фото (у которых есть локальный файл) */
    suspend fun retrySyncFailedPhotos()
}
