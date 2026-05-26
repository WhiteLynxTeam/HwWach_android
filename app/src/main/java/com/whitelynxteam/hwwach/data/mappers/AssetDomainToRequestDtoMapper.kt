package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.asset.AssetRequestDto
import com.whitelynxteam.hwwach.domain.models.Asset
import java.util.Locale
import javax.inject.Inject

class AssetDomainToRequestDtoMapper @Inject constructor() {
    fun map(domain: Asset): AssetRequestDto {
        return AssetRequestDto(
            clientId = domain.clientId,
            inventoryNum = domain.inventoryNum,
            description = domain.description,
            assetStatus = domain.assetStatus?.name?.lowercase(Locale.ROOT) ?: "active",
            category = domain.category ?: "",
            name = domain.name,
            photoClientIds = domain.photoClientIds
        )
    }
}
