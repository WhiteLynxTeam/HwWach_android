package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import com.whitelynxteam.hwwach.data.mappers.PhotoDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoDtoToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoEntityToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.ResponseErrorMapper
import com.whitelynxteam.hwwach.data.remote.api.PhotosApi
import com.whitelynxteam.hwwach.data.remote.model.photo.CompleteUploadRequest
import com.whitelynxteam.hwwach.data.remote.model.photo.UploadUrlRequest
import com.whitelynxteam.hwwach.data.remote.model.photo.UploadUrlResponse
import com.whitelynxteam.hwwach.di.UploadOkHttpClient
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.istorage.IFileStorage
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class PhotoRepositoryImpl @Inject constructor(
    @Named("api") private val photosApi: PhotosApi,
    private val photoDao: PhotoDao,
    private val photoDtoToDomainMapper: PhotoDtoToDomainMapper,
    private val photoDomainToEntityMapper: PhotoDomainToEntityMapper,
    private val photoEntityToDomainMapper: PhotoEntityToDomainMapper,
    private val responseErrorMapper: ResponseErrorMapper,
    @UploadOkHttpClient private val uploadClient: OkHttpClient,
    private val fileStorage: IFileStorage,
) : IPhotoRepository {

    /**
     * Синхронизация фото с сервером (SSOT через Flow из БД).
     * Не возвращает данные — UI получает их через getAllPhotosFlow().
     *
     * Логика синхронизации:
     * 1. Получает список фото с сервера
     * 2. Удаляет из БД SYNCED фото, которых нет на сервере
     * 3. Обновляет/вставляет фото с сервера, сохраняя localFilePath
     */
    override suspend fun syncPhotos(): DomainResult<Unit> {
        val response = photosApi.photos()

        return when {
            !response.isSuccessful -> responseErrorMapper.map(response)
            else -> {
                val photosResponseDto = response.body()
                    ?: return DomainResult.ValidationError("Photos response not found")

                // ЛОГ 1: Сырые данные с сервера
                println("[SYNC_PHOTOS] Raw data from server: ${photosResponseDto.photos.size} photos")
                photosResponseDto.photos.forEachIndexed { index, dto ->
                    println("[SYNC_PHOTOS] Photo[$index]: clientId=${dto.clientId}, uuid=${dto.uuid}")
                    println("[SYNC_PHOTOS] Photo[$index] URL: ${dto.url}")
                }

                // Фиксим фото без clientId — используем serverUuid как fallback
                val fixedPhotos = photosResponseDto.photos.map { dto ->
                    if (dto.clientId == null) {
                        println("[SYNC_PHOTOS] WARNING: Photo has null clientId, using uuid as fallback! uuid=${dto.uuid}")
                        // Создаём копию DTO с clientId = uuid
                        dto.copy(clientId = dto.uuid)
                    } else {
                        dto
                    }
                }

                val nullClientIdCount = photosResponseDto.photos.count { it.clientId == null }
                if (nullClientIdCount > 0) {
                    println("[SYNC_PHOTOS] Fixed $nullClientIdCount photos with null clientId (used uuid as fallback)")
                }

                // Маппинг через Domain-слой (DTO → Domain → Entity) изолирует
                // структуру локальной БД от сетевых ответов и переиспользует готовые мапперы.
                val serverEntities = fixedPhotos.map { photoDto ->
                    val domainPhoto = photoDtoToDomainMapper.map(photoDto)
                    val entityPhoto = photoDomainToEntityMapper.map(domainPhoto)
                    // ЛОГ 2: После двух маппингов
                    println("[SYNC_PHOTOS] After mapping: clientId=${entityPhoto.clientId}, serverUuid=${entityPhoto.serverUuid}, localPath=${entityPhoto.localFilePath}")
                    entityPhoto
                }

                println("[SYNC_PHOTOS] Total entities to sync: ${serverEntities.size}")

                // Умная синхронизация: удаляет лишние SYNCED, сохраняет localFilePath
                photoDao.smartSyncPhotos(serverEntities)

                // Успех — данные уже в БД, Flow их доставит в UI
                DomainResult.Success(Unit)
            }
        }
    }

    override fun getPhotosFlow(clientId: String): Flow<List<Photo>> {
        return photoDao.getPhotosByClientId(clientId).map { entities ->
            entities.map { entity ->
                val actualLocalPath = entity.localFilePath?.takeIf { fileStorage.fileExists(it) }
                photoEntityToDomainMapper.map(entity.copy(localFilePath = actualLocalPath))
            }
        }
    }

    override fun getAllPhotosFlow(): Flow<List<Photo>> {
        return photoDao.getAllPhotos().map { entities ->
            entities.map { entity ->
                val actualLocalPath = entity.localFilePath?.takeIf { fileStorage.fileExists(it) }
                photoEntityToDomainMapper.map(entity.copy(localFilePath = actualLocalPath))
            }
        }
    }

    override fun getPhotosByStatusFlow(status: String): Flow<List<Photo>> {
        return photoDao.getPhotosByStatus(status).map { entities ->
            entities.map { entity ->
                val actualLocalPath = entity.localFilePath?.takeIf { fileStorage.fileExists(it) }
                photoEntityToDomainMapper.map(entity.copy(localFilePath = actualLocalPath))
            }
        }
    }

    override suspend fun savePhoto(photo: Photo) {
        val localPath = fileStorage.copyToCache(photo.localPath ?: "")
        val entity = photoDomainToEntityMapper.map(photo.copy(localPath = localPath))
        photoDao.insertPhoto(entity)
    }

    override suspend fun deletePhoto(clientId: String) {
        val photo = photoDao.getPhotoByClientId(clientId)
        photo?.localFilePath?.let { fileStorage.deleteFile(it) }
        photoDao.deletePhotoByClientId(clientId)
    }

    override suspend fun getPendingPhotos(): List<Photo> {
        return photoDao.getPendingPhotos().map { photoEntityToDomainMapper.map(it) }
    }

    override suspend fun syncPendingPhotos() = withContext(Dispatchers.IO) {
        val pendingPhotos = photoDao.getPendingPhotos()
        if (pendingPhotos.isEmpty()) return@withContext

        for (entity in pendingPhotos) {
            currentCoroutineContext().ensureActive()

            val localPath = entity.localFilePath ?: continue
            val bytes = fileStorage.readBytes(localPath) ?: continue

            val result = uploadSinglePhoto(entity, bytes)
            if (result is DomainResult.Success) {
                photoDao.completeUpload(
                    entity.clientId,
                    result.data,
                    PhotoUploadStatusEnum.UPLOADED.name
                )

                // Подтверждаем получение файла бэкендом
                confirmUploadToBackend(entity.clientId, result.data)
            }
        }
    }

    override suspend fun resumeUploadedPhotos() = withContext(Dispatchers.IO) {
        val uploadedPhotos = photoDao.getUploadedPhotos()
        for (entity in uploadedPhotos) {
            val serverUuid = entity.serverUuid ?: continue
            confirmUploadToBackend(entity.clientId, serverUuid)
        }
    }

    override suspend fun resetStuckUploads() = withContext(Dispatchers.IO) {
        val stuckPhotos = photoDao.getStuckUploadingPhotos()
        for (entity in stuckPhotos) {
            // Если локальный файл есть — сбрасываем в FAILED для повторной отправки
            // Если локального файла нет — возможно он был удалён до краша, помечаем как FAILED
            photoDao.updateStatusWithError(
                entity.clientId,
                PhotoUploadStatusEnum.FAILED.name,
                "Загрузка прервана (UPLOADING при старте приложения)"
            )
        }
    }

    override suspend fun retrySyncFailedPhotos() = withContext(Dispatchers.IO) {
        val retryablePhotos = photoDao.getRetryablePhotos()
        for (entity in retryablePhotos) {
            currentCoroutineContext().ensureActive()

            val localPath = entity.localFilePath ?: continue
            val bytes = fileStorage.readBytes(localPath) ?: continue

            // Сбрасываем статус на PENDING для повторной отправки через uploadSinglePhoto
            photoDao.updatePhotoStatus(entity.clientId, PhotoUploadStatusEnum.PENDING.name)
            val result = uploadSinglePhoto(entity, bytes)
            if (result is DomainResult.Success) {
                photoDao.completeUpload(
                    entity.clientId,
                    result.data,
                    PhotoUploadStatusEnum.UPLOADED.name
                )
                confirmUploadToBackend(entity.clientId, result.data)
            }
        }
    }

    private suspend fun confirmUploadToBackend(clientId: String, photoUuid: String): Boolean {
        return try {
            val response = photosApi.completeUpload(CompleteUploadRequest(photoUuid = photoUuid))
            if (response.isSuccessful) {
                val photoDto = response.body()
                if (photoDto != null) {
                    photoDao.completeUploadWithUrl(
                        clientId = clientId,
                        serverUuid = photoDto.uuid,
                        remoteUrl = photoDto.url,
                        status = PhotoUploadStatusEnum.SYNCED.name
                    )
                    true
                } else {
                    false
                }
            } else {
                false
            }
            // Если не удалось — фото остаётся в UPLOADED, resumeUploadedPhotos() повторит попытку при следующем открытии
        } catch (_: Exception) {
            // Сеть недоступна — остаёмся в UPLOADED, повторим при следующем открытии
            false
        }
    }

    private suspend fun uploadSinglePhoto(entity: PhotoEntity, fileBytes: ByteArray): DomainResult<String> {
        val filename = entity.localFilePath?.substringAfterLast("/") ?: "photo.jpg"
        val contentType = guessContentType(filename)

        val urlResponse: Response<UploadUrlResponse> = try {
            photosApi.getUploadUrl(
                UploadUrlRequest(
                    clientId = entity.clientId,
                    contentType = contentType,
                    fileSize = fileBytes.size.toLong(),
                    filename = filename,
                )
            )
        } catch (e: Exception) {
            photoDao.updateStatusWithError(
                entity.clientId,
                PhotoUploadStatusEnum.FAILED.name,
                e.message ?: "Failed to get upload URL"
            )
            return DomainResult.NetworkError(e.message ?: "Failed to get upload URL")
        }

        if (!urlResponse.isSuccessful || urlResponse.body() == null) {
            val errorMsg = "Failed to get upload URL"
            photoDao.updateStatusWithError(entity.clientId, PhotoUploadStatusEnum.FAILED.name, errorMsg)
            return DomainResult.ValidationError(errorMsg)
        }

        val uploadUrl = urlResponse.body()!!.uploadUrl

        photoDao.updatePhotoStatus(entity.clientId, PhotoUploadStatusEnum.UPLOADING.name)

        val uploadResponse = try {
            val mediaType = contentType.toMediaType()
            val request = okhttp3.Request.Builder()
                .url(uploadUrl)
                .put(fileBytes.toRequestBody(mediaType))
                .build()

            uploadClient.newCall(request).execute()
        } catch (e: Exception) {
            photoDao.updateStatusWithError(
                entity.clientId,
                PhotoUploadStatusEnum.FAILED.name,
                e.message ?: "Upload failed"
            )
            return DomainResult.NetworkError(e.message ?: "Upload failed")
        }

        if (!uploadResponse.isSuccessful) {
            val errorMsg = "Upload failed: ${uploadResponse.code}"
            photoDao.updateStatusWithError(entity.clientId, PhotoUploadStatusEnum.FAILED.name, errorMsg)
            return DomainResult.NetworkError(errorMsg)
        }

        return DomainResult.Success(urlResponse.body()!!.photoUuid)
    }

    private fun guessContentType(filename: String): String {
        return when {
            filename.endsWith(".jpg", ignoreCase = true) ||
                    filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"

            filename.endsWith(".png", ignoreCase = true) -> "image/png"
            filename.endsWith(".webp", ignoreCase = true) -> "image/webp"
            filename.endsWith(".gif", ignoreCase = true) -> "image/gif"
            else -> "application/octet-stream"
        }
    }
}
