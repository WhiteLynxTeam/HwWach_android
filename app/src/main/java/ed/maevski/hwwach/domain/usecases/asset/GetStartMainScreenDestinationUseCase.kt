package ed.maevski.hwwach.domain.usecases.asset

import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.models.MainDestinationEnum
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