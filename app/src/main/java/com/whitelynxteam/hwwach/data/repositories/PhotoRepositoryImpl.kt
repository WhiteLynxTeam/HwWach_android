package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import com.whitelynxteam.hwwach.data.mappers.PhotoDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoDtoToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoEntityToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.ResponseErrorMapper
import com.whitelynxteam.hwwach.data.remote.api.PhotosApi
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

    override suspend fun getPhotos(clientId: String): DomainResult<List<Photo>> {
        val response = photosApi.photos()

        return when {
            !response.isSuccessful -> responseErrorMapper.map(response)
            else -> {
                val photosResponseDto = response.body()
                    ?: return DomainResult.ValidationError("Photos response not found")

                val photos = photosResponseDto.photos.map { photoDto ->
                    photoDtoToDomainMapper.map(photoDto)
                }

                val entities = photos.map { photoDomainToEntityMapper.map(it) }
                photoDao.insertPhotos(entities)

                DomainResult.Success(photos)
            }
        }
    }

    override fun getPhotosFlow(clientId: String): Flow<List<Photo>> {
        return photoDao.getPhotosByClientId(clientId).map { entities ->
            entities.map { photoEntityToDomainMapper.map(it) }
        }
    }

    override fun getOrphanPhotosFlow(): Flow<List<Photo>> {
        return photoDao.getOrphanPhotos().map { entities ->
            entities.map { photoEntityToDomainMapper.map(it) }
        }
    }

    override suspend fun savePhoto(photo: Photo) {
        val localPath = fileStorage.copyToCache(photo.localPath ?: "")
        val entity = photoDomainToEntityMapper.map(photo.copy(localPath = localPath))
        photoDao.insertPhoto(entity)
    }

    override suspend fun deletePhoto(clientId: String) {
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
                    PhotoUploadStatusEnum.SYNCED.name
                )
            }
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
            return DomainResult.NetworkError(e.message ?: "Failed to get upload URL")
        }

        if (!urlResponse.isSuccessful || urlResponse.body() == null) {
            return DomainResult.ValidationError("Failed to get upload URL")
        }

        val uploadUrl = urlResponse.body()!!.uploadUrl

        val uploadResponse = try {
            val mediaType = contentType.toMediaType()
            val request = okhttp3.Request.Builder()
                .url(uploadUrl)
                .put(fileBytes.toRequestBody(mediaType))
                .build()

            uploadClient.newCall(request).execute()
        } catch (e: Exception) {
            return DomainResult.NetworkError(e.message ?: "Upload failed")
        }

        if (!uploadResponse.isSuccessful) {
            return DomainResult.NetworkError("Upload failed: ${uploadResponse.code}")
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
