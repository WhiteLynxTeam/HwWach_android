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
            inventoryNumber = entity.inventoryNumber,
            description = entity.description,
            localCreatedAt = entity.localCreatedAt,
            status = entity.status,
            lastUpdatedLocally = entity.lastUpdatedLocally,
        )
    }
}
