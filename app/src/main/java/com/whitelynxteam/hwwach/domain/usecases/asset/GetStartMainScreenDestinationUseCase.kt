package com.whitelynxteam.hwwach.domain.usecases.asset

import com.whitelynxteam.hwwach.domain.irepositories.IAssetRepository
import com.whitelynxteam.hwwach.domain.models.MainDestinationEnum
import javax.inject.Inject

class GetStartMainScreenDestinationUseCase @Inject constructor(
    private val assetRepository: IAssetRepository
) {
    suspend operator fun invoke(): MainDestinationEnum {
        return when {
            assetRepository.hasAssets() > 0 -> MainDestinationEnum.ASSET_SCREEN
            else -> MainDestinationEnum.PHOTO_SCREEN
        }
    }
}