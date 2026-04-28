package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.photo.PhotoDto
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum
import javax.inject.Inject

class PhotoDtoToDomainMapper @Inject constructor() {
    fun map(photoDto: PhotoDto): Photo {
        return Photo(
            clientId = photoDto.clientId,
            serverUuid = photoDto.uuid,
            localCreatedAt = System.currentTimeMillis(),
            status = PhotoUploadStatusEnum.SYNCED,
            localPath = null,
            remoteUrl = photoDto.url,
            errorMessage = null
        )
    }
}
