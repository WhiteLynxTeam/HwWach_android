package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.irepositories.IDeviceRepository
import com.whitelynxteam.hwwach.domain.models.MainDestinationEnum
import javax.inject.Inject

class GetStartMainScreenDestinationUseCase @Inject constructor(
    private val deviceRepository: IDeviceRepository) {
    suspend operator fun invoke(): MainDestinationEnum {
        return when {
            deviceRepository.hasDevices() > 0 -> MainDestinationEnum.DEVICE_SCREEN
            else -> MainDestinationEnum.PHOTO_SCREEN
        }
    }
}