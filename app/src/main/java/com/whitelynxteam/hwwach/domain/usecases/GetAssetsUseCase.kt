package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IAssetRepository
import com.whitelynxteam.hwwach.domain.models.Asset
import javax.inject.Inject

class GetAssetsUseCase @Inject constructor(
    private val assetRepository: IAssetRepository
) {
    suspend operator fun invoke(): DomainResult<List<Asset>> {
        return assetRepository.getAssets()
    }
}
