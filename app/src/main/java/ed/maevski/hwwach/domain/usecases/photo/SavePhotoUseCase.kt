package ed.maevski.hwwach.domain.usecases.photo

import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.models.Photo
import javax.inject.Inject

class SavePhotoUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    suspend operator fun invoke(photo: Photo) {
        photoRepository.savePhoto(photo)
    }
}
