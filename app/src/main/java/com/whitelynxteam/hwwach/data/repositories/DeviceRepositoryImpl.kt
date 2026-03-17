package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.dao.DeviceDao
import com.whitelynxteam.hwwach.data.mappers.DeviceDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.DeviceEntityToDomainMapper
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IDeviceRepository
import com.whitelynxteam.hwwach.domain.models.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao,
    private val deviceEntityToDomainMapper: DeviceEntityToDomainMapper,
    private val deviceDomainToEntityMapper: DeviceDomainToEntityMapper,
) : IDeviceRepository {

    override suspend fun hasDevices(): Int {
        return deviceDao.hasDevices()
    }

    override suspend fun getDevices(): DomainResult<List<Device>> {
        // TODO: реализовать получение из API
        return DomainResult.Success(emptyList())
    }

    override fun getDevicesFlow(): Flow<List<Device>> {
        return deviceDao.getAllDevices().map { entities ->
            entities.map { deviceEntityToDomainMapper.map(it) }
        }
    }

    override fun getDeviceFlow(clientId: String): Flow<Device?> {
        return deviceDao.getDeviceByClientId(clientId).map { entity ->
            entity?.let { deviceEntityToDomainMapper.map(it) }
        }
    }

    suspend fun saveDevice(device: Device) {
        val entity = deviceDomainToEntityMapper.map(device)
        deviceDao.insertDevice(entity)
    }

    suspend fun saveDevices(devices: List<Device>) {
        val entities = devices.map { deviceDomainToEntityMapper.map(it) }
        deviceDao.insertDevices(entities)
    }
}
