package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.asset.AssetRequestDto
import ed.maevski.hwwach.domain.models.Asset
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
