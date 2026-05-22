package com.whitelynxteam.hwwach.domain.usecases.asset

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IAssetRepository
import com.whitelynxteam.hwwach.domain.models.Asset
import javax.inject.Inject

class AddAssetUseCase @Inject constructor(
    private val assetRepository: IAssetRepository,
) {
    suspend operator fun invoke(asset: Asset): DomainResult<Asset> {
        return assetRepository.addAsset(asset)
    }
}
