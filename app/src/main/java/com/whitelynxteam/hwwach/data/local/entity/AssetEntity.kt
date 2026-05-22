package com.whitelynxteam.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.whitelynxteam.hwwach.domain.models.AssetStatusEnum
import com.whitelynxteam.hwwach.domain.models.ModerationStatusEnum
import com.whitelynxteam.hwwach.domain.models.UploadStatusEnum

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val clientId: String, // UUID v7
    val serverUuid: String? = null,

    // Бизнес-данные
    val name: String,
    val category: String?,
    val inventoryNum: String?,
    val description: String?,

    // Статусы
    val assetStatus: AssetStatusEnum?,
    val moderationStatus: ModerationStatusEnum = ModerationStatusEnum.PENDING,
    val status: UploadStatusEnum = UploadStatusEnum.PENDING,
    val adminComment: String? = null,

    // Временны́е метки
    val createdAt: Long? = null,
    val updatedAt: Long? = null,

    // Технические поля (те же, что у фото)
    val localCreatedAt: Long,

    // Поле для отслеживания локальных изменений
    val lastUpdatedLocally: Long
)
