package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.local.entity.PhotoEntity
import com.whitelynxteam.hwwach.domain.models.Photo
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
        )
    }
}
