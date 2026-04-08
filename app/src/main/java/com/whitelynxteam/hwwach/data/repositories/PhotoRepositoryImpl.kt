package com.whitelynxteam.hwwach.data.repositories

import android.content.Context
import android.net.Uri
import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import com.whitelynxteam.hwwach.data.mappers.PhotoDtoToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoEntityToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.ResponseErrorMapper
import com.whitelynxteam.hwwach.data.remote.api.PhotosApi
import com.whitelynxteam.hwwach.data.remote.model.photo.UploadUrlRequest
import com.whitelynxteam.hwwach.data.remote.model.photo.UploadUrlResponse
import com.whitelynxteam.hwwach.di.UploadOkHttpClient
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url
import java.io.File
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
    @ApplicationContext private val context: Context,
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
        val localPath = copyToCache(photo.localPath)
        val entity = photoDomainToEntityMapper.map(photo.copy(localPath = localPath))
        photoDao.insertPhoto(entity)
    }

    override suspend fun deletePhoto(clientId: String) {
        photoDao.deletePhotoByClientId(clientId)
    }

    override suspend fun getPendingPhotos(): List<Photo> {
        return photoDao.getPendingPhotos().map { photoEntityToDomainMapper.map(it) }
    }

    override suspend fun syncPendingPhotos() {
        val pendingPhotos = photoDao.getPendingPhotos()
        if (pendingPhotos.isEmpty()) return

        for (entity in pendingPhotos) {
            currentCoroutineContext().ensureActive() // Проверка отмены

            val localPath = entity.localFilePath ?: continue
            val bytes = try {
                val uri = Uri.parse(localPath)
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                null
            }

            if (bytes == null) continue

            val result = uploadSinglePhoto(entity, bytes)
            if (result is DomainResult.Success) {
                photoDao.completeUpload(
                    entity.clientId,
                    (result.data as String),
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

        val uploadResponse: Response<Void> = try {
            val uploadApi = Retrofit.Builder()
                .baseUrl("$uploadUrl/")
                .client(uploadClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create<UploadPhotosApi>()

            uploadApi.uploadPhotoToUrl(uploadUrl, fileBytes.toRequestBody(null))
        } catch (e: Exception) {
            return DomainResult.NetworkError(e.message ?: "Upload failed")
        }

        if (!uploadResponse.isSuccessful) {
            return DomainResult.NetworkError("Upload failed: ${uploadResponse.code()}")
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

    /** Копирует `content://` URI в `cacheDir`, чтобы пережить перезапуск приложения */
    private suspend fun copyToCache(uri: String?): String {
        if (uri == null) return ""
        val contentUri = Uri.parse(uri)
        if (!contentUri.scheme.equals("content", ignoreCase = true)) return uri

        val inputStream = context.contentResolver.openInputStream(contentUri)
            ?: throw IllegalStateException("Cannot open InputStream for $uri")
        val dir = File(context.cacheDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        val outFile = File(dir, "${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(outFile).toString()
    }
}

private interface UploadPhotosApi {
    @PUT
    suspend fun uploadPhotoToUrl(
        @Url uploadUrl: String,
        @Body file: okhttp3.RequestBody,
    ): Response<Void>
}
