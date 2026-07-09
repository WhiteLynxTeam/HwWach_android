package ed.maevski.hwwach.domain.usecases.photo

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import javax.inject.Inject

/**
 * Use case для синхронизации фото с сервером.
 * Не возвращает данные — UI должен использовать Flow из getAllPhotosFlow() для получения фото.
 */
class SyncPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return photoRepository.syncPhotos()
    }
}
