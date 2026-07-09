package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.photo.PhotoDto
import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import javax.inject.Inject

class PhotoDtoToDomainMapper @Inject constructor() {
    fun map(photoDto: PhotoDto): Photo {
        return Photo(
            clientId = photoDto.clientId,
            serverUuid = photoDto.uuid,
            localCreatedAt = System.currentTimeMillis(),
            status = UploadStatusEnum.SYNCED,
            localPath = null,
            remoteUrl = photoDto.url,
            errorMessage = null
        )
    }
}
