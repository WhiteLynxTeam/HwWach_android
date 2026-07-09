package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.local.entity.PhotoEntity
import ed.maevski.hwwach.domain.models.Photo
import javax.inject.Inject

class PhotoEntityToDomainMapper @Inject constructor() {
    fun map(entity: PhotoEntity): Photo {
        return Photo(
            clientId = entity.clientId,
            serverUuid = entity.serverUuid,
            localCreatedAt = entity.localCreatedAt,
            status = entity.status,
            localPath = entity.localFilePath,
            remoteUrl = entity.remoteUrl,
            errorMessage = entity.errorMessage
        )
    }
}
