package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.asset.AssetRequestDto
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetStatusEnum
import javax.inject.Inject

class AssetDomainToRequestDtoMapper @Inject constructor() {
    fun map(domain: Asset): AssetRequestDto {
        return AssetRequestDto(
            clientId = domain.clientId,
            inventoryNum = domain.inventoryNum,
            description = domain.description,
            // новый актив не бывает «в ремонте» или списанным — такие статусы сервер не примет
            accountingStatus = when (domain.assetStatus) {
                AssetStatusEnum.INACTIVE -> "in_stock"
                AssetStatusEnum.LOST -> "lost"
                else -> "in_use"
            },
            category = domain.category,
            categoryUuid = domain.categoryUuid,
            categoryText = if (domain.categoryUuid == null) domain.category else null,
            name = domain.name,
            photoClientIds = domain.photoClientIds
        )
    }
}
