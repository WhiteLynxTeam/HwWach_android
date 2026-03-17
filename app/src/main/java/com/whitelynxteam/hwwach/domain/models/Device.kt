package com.whitelynxteam.hwwach.domain.models

data class Device(
    val clientId: String, // UUID v7
    val serverUuid: String?,

    // Бизнес-данные
    val name: String,
    val inventoryNumber: String?,
    val description: String?,

    // Технические поля
    val localCreatedAt: Long,
    val status: DeviceUploadStatusEnum,

    // Поле для отслеживания локальных изменений
    val lastUpdatedLocally: Long
) {
    // Бизнес-свойство: синхронизировано ли устройство?
    val isSyncedToServer: Boolean get() = serverUuid != null

    // Бизнес-свойство: можем ли мы работать с этим устройством через API?
    val isRemoteReady: Boolean get() = isSyncedToServer
}
