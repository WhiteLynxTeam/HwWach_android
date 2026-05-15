package com.whitelynxteam.hwwach.domain.models

data class Asset(
    val clientId: String, // UUID v7
    val serverUuid: String?,

    // Бизнес-данные
    val name: String,
    val inventoryNumber: String?,
    val description: String?,

    // Технические поля
    val localCreatedAt: Long,
    val status: AssetUploadStatusEnum,

    // Поле для отслеживания локальных изменений
    val lastUpdatedLocally: Long
) {
    // Бизнес-свойство: синхронизирован ли актив?
    val isSyncedToServer: Boolean get() = serverUuid != null

    // Бизнес-свойство: можем ли мы работать с этим активом через API?
    val isRemoteReady: Boolean get() = isSyncedToServer
}
