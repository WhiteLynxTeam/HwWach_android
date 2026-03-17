package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.Device
import kotlinx.coroutines.flow.Flow

interface IDeviceRepository {

    suspend fun hasDevices(): Int
    suspend fun getDevices(): DomainResult<List<Device>>
    fun getDevicesFlow(): Flow<List<Device>>
    fun getDeviceFlow(clientId: String): Flow<Device?>
}
