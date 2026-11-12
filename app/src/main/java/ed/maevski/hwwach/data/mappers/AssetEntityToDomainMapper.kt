package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.local.entity.AssetEntity
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.MolMode
import javax.inject.Inject

class AssetEntityToDomainMapper @Inject constructor() {
    fun map(entity: AssetEntity): Asset {
        return Asset(
            clientId = entity.clientId,
            serverUuid = entity.serverUuid,
            name = entity.name,
            category = entity.category,
            categoryUuid = entity.categoryUuid,
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
            locationKind = AssetLocationKind.fromServer(entity.locationKind),
            locationId = entity.locationId,
            locationPath = entity.locationPath,
            molMode = runCatching { MolMode.valueOf(entity.molMode) }.getOrDefault(MolMode.DEFAULT),
            molPersonId = entity.molPersonId,
            errorMessage = entity.errorMessage,
        )
    }
}
