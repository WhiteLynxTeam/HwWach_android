package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.local.entity.AssetEntity
import com.whitelynxteam.hwwach.domain.models.Asset
import javax.inject.Inject

class AssetEntityToDomainMapper @Inject constructor() {
    fun map(entity: AssetEntity): Asset {
        return Asset(
            clientId = entity.clientId,
            serverUuid = entity.serverUuid,
            name = entity.name,
            category = entity.category,
            inventoryNum = entity.inventoryNum,
            description = entity.description,
            assetStatus = entity.assetStatus,
            moderationStatus = entity.moderationStatus,
            adminComment = entity.adminComment,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            localCreatedAt = entity.localCreatedAt,
            lastUpdatedLocally = entity.lastUpdatedLocally,
            status = entity.status,
        )
    }
}
