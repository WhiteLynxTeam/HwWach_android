package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.local.entity.AssetEntity
import com.whitelynxteam.hwwach.domain.models.Asset
import javax.inject.Inject

class AssetDomainToEntityMapper @Inject constructor() {
    fun map(asset: Asset): AssetEntity {
        return AssetEntity(
            clientId = asset.clientId,
            serverUuid = asset.serverUuid,
            name = asset.name,
            category = asset.category,
            inventoryNum = asset.inventoryNum,
            description = asset.description,
            assetStatus = asset.assetStatus,
            moderationStatus = asset.moderationStatus ?: com.whitelynxteam.hwwach.domain.models.ModerationStatusEnum.PENDING,
            adminComment = asset.adminComment,
            createdAt = asset.createdAt,
            updatedAt = asset.updatedAt,
            localCreatedAt = asset.localCreatedAt,
            lastUpdatedLocally = asset.lastUpdatedLocally,
            status = asset.status,
        )
    }
}
