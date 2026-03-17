package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow

interface IPhotoRepository {
    suspend fun getPhotos(clientId: String): DomainResult<List<Photo>>
    fun getPhotosFlow(clientId: String): Flow<List<Photo>>
}
