package com.whitelynxteam.hwwach.domain.usecases.photo

import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import javax.inject.Inject

/**
 * Повторяет отправку фото со статусом FAILED у которых сохранился локальный файл.
 * Фото без локального файла пропускаются (файл был удалён или не был сохранён).
 */
class RetrySyncFailedPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository,
) {
    suspend operator fun invoke() {
        photoRepository.retrySyncFailedPhotos()
    }
}
