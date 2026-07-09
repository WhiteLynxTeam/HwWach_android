package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.local.entity.PhotoEntity
import ed.maevski.hwwach.domain.models.Photo
import javax.inject.Inject

class PhotoDomainToEntityMapper @Inject constructor() {
    fun map(photo: Photo): PhotoEntity {
        return PhotoEntity(
            clientId = photo.clientId,
            serverUuid = photo.serverUuid,
            localFilePath = photo.localPath,
            remoteUrl = photo.remoteUrl,
            localCreatedAt = photo.localCreatedAt,
            status = photo.status,
            errorMessage = photo.errorMessage
        )
    }
}
