package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.asset.AssetRequestDto
import ed.maevski.hwwach.data.remote.model.asset.LocationNodeDto
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.MolMode
import ed.maevski.hwwach.domain.models.AssetStatusEnum
import javax.inject.Inject

class AssetDomainToRequestDtoMapper @Inject constructor() {
    fun map(domain: Asset, chain: List<Location> = emptyList()): AssetRequestDto {
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
            photoClientIds = domain.photoClientIds,
            molPersonId = when (domain.molMode) {
                MolMode.DEFAULT -> null
                MolMode.NONE -> ""
                MolMode.PERSON -> domain.molPersonId
            },
            locationKind = domain.locationKind.server,
            location = if (domain.locationKind == AssetLocationKind.PLACE) chain.toNodes() else null,
        )
    }
}


fun List<Location>.toNodes(): List<LocationNodeDto> = map { LocationNodeDto(id = it.id, name = it.name, kind = it.kind.server) }
