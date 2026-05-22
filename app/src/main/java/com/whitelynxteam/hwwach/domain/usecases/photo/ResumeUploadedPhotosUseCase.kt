package com.whitelynxteam.hwwach.domain.usecases.photo

import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import javax.inject.Inject

/**
 * Завершает подтверждение для фото со статусом UPLOADED.
 * Вызывает POST /photos/complete-upload для каждого такого фото,
 * чтобы бэкенд зафиксировал файл в своей БД и переключил статус в SYNCED.
 */
class ResumeUploadedPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository,
) {
    suspend operator fun invoke() {
        photoRepository.resumeUploadedPhotos()
    }
}
