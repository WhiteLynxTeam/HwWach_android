package ed.maevski.hwwach.domain.usecases.photo

import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.models.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPhotosUseCase @Inject constructor(
    private val photoRepository: IPhotoRepository
) {
    operator fun invoke(): Flow<List<Photo>> {
        return photoRepository.getAllPhotosFlow()
    }
}
