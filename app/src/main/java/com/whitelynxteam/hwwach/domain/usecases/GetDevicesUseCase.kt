package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IDeviceRepository
import com.whitelynxteam.hwwach.domain.models.Device
import javax.inject.Inject

class GetDevicesUseCase @Inject constructor(
    private val deviceRepository: IDeviceRepository
) {
    suspend operator fun invoke(): DomainResult<List<Device>> {
        return deviceRepository.getDevices()
    }
}
