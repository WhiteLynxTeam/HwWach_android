package com.whitelynxteam.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.whitelynxteam.hwwach.domain.models.UploadStatusEnum

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val clientId: String,
    val serverUuid: String? = null,

    val localFilePath: String? = null,
    val remoteUrl: String? = null,

    val localCreatedAt: Long,
    val remoteCreatedAt: Long? = null,
    val status: UploadStatusEnum = UploadStatusEnum.PENDING,

    val errorMessage: String? = null
)
