package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.dao.PhotoDao
import com.whitelynxteam.hwwach.data.mappers.PhotoDtoToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.PhotoEntityToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.ResponseErrorMapper
import com.whitelynxteam.hwwach.data.remote.api.PhotosApi
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val photosApi: PhotosApi,
    private val photoDao: PhotoDao,
    private val photoDtoToDomainMapper: PhotoDtoToDomainMapper,
    private val photoDomainToEntityMapper: PhotoDomainToEntityMapper,
    private val photoEntityToDomainMapper: PhotoEntityToDomainMapper,
    private val responseErrorMapper: ResponseErrorMapper,
) : IPhotoRepository {

    // TODO: продумать логику получения clientId
    private val clientIdStub: String = "stub-client-id"

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

                // Сохраняем в локальную БД
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
}
