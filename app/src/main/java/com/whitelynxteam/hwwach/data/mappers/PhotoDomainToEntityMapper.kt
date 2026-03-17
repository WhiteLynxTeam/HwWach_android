package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import com.whitelynxteam.hwwach.domain.models.Photo
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
        )
    }
}
