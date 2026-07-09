package ed.maevski.hwwach.domain.usecases.photo

import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import javax.inject.Inject

class SyncPendingPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository,
) {
    suspend operator fun invoke() {
        photoRepository.syncPendingPhotos()
    }
}
