package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.local.entity.DeviceEntity
import com.whitelynxteam.hwwach.domain.models.Device
import javax.inject.Inject

class DeviceEntityToDomainMapper @Inject constructor() {
    fun map(entity: DeviceEntity): Device {
        return Device(
            clientId = entity.clientId,
            serverUuid = entity.serverUuid,
            name = entity.name,
            inventoryNumber = entity.inventoryNumber,
            description = entity.description,
            localCreatedAt = entity.localCreatedAt,
            status = entity.status,
            lastUpdatedLocally = entity.lastUpdatedLocally,
        )
    }
}
