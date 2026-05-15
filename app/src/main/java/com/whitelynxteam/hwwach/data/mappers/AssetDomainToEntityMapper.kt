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
            inventoryNumber = asset.inventoryNumber,
            description = asset.description,
            localCreatedAt = asset.localCreatedAt,
            status = asset.status,
            lastUpdatedLocally = asset.lastUpdatedLocally,
        )
    }
}
