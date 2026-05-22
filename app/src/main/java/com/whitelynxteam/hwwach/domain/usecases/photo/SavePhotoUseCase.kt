package com.whitelynxteam.hwwach.domain.usecases.photo

import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Photo
import javax.inject.Inject

class SavePhotoUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    suspend operator fun invoke(photo: Photo) {
        photoRepository.savePhoto(photo)
    }
}
