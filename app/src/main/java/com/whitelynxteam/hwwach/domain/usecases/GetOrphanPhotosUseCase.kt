package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrphanPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    operator fun invoke(): Flow<List<Photo>> {
        return photoRepository.getOrphanPhotosFlow()
    }
}