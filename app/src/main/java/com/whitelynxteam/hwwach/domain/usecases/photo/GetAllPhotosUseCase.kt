package com.whitelynxteam.hwwach.domain.usecases.photo

import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    operator fun invoke(): Flow<List<Photo>> {
        return photoRepository.getAllPhotosFlow()
    }
}
