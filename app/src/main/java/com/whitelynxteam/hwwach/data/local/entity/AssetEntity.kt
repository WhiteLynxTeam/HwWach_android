package com.whitelynxteam.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.whitelynxteam.hwwach.domain.models.AssetUploadStatusEnum

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val clientId: String, // UUID v7
    val serverUuid: String? = null,

    // Бизнес-данные
    val name: String,
    val inventoryNumber: String?,
    val description: String?,

    // Технические поля (те же, что у фото)
    val localCreatedAt: Long,
    val remoteCreatedAt: Long? = null,
    val status: AssetUploadStatusEnum = AssetUploadStatusEnum.PENDING,

    // Поле для отслеживания локальных изменений
    val lastUpdatedLocally: Long
)
