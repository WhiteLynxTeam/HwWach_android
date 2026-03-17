package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Photo
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    suspend operator fun invoke(clientId: String): DomainResult<List<Photo>> {
        return photoRepository.getPhotos(clientId)
    }
}
