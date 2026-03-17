package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.local.entity.DeviceEntity
import com.whitelynxteam.hwwach.domain.models.Device
import javax.inject.Inject
class DeviceDomainToEntityMapper @Inject constructor() {
    fun map(device: Device): DeviceEntity {
        return DeviceEntity(
            clientId = device.clientId,
            serverUuid = device.serverUuid,
            name = device.name,
            inventoryNumber = device.inventoryNumber,
            description = device.description,
            localCreatedAt = device.localCreatedAt,
            status = device.status,
            lastUpdatedLocally = device.lastUpdatedLocally,
        )
    }
}
