package com.whitelynxteam.hwwach.domain.usecases.photo

import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import javax.inject.Inject

/**
 * Сбрасывает фото, зависшие на статусе UPLOADING, в FAILED.
 * Это происходит когда приложение упало или потеряло связь в момент загрузки в MinIO.
 * После сброса фото будут повторно отправлены через retrySyncFailedPhotos.
 */
class ResetStuckUploadsUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository,
) {
    suspend operator fun invoke() {
        photoRepository.resetStuckUploads()
    }
}
