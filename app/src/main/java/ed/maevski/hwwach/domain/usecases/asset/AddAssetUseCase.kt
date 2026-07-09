package ed.maevski.hwwach.domain.usecases.asset

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.models.Asset
import javax.inject.Inject

class AddAssetUseCase @Inject constructor(
    private val assetRepository: IAssetRepository,
) {
    suspend operator fun invoke(asset: Asset): DomainResult<Asset> {
        return assetRepository.addAsset(asset)
    }
}
