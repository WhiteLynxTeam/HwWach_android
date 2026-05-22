package com.whitelynxteam.hwwach.domain.models

data class Asset(
    val clientId: String,       // UUID v7, генерируется локально
    val serverUuid: String?,    // UUID от бэкенда, null пока не синхронизирован

    // Бизнес-данные
    val name: String,
    val category: String?,
    val inventoryNum: String?,
    val description: String?,

    // Статусы — теперь два явных поля
    val assetStatus: AssetStatusEnum?,      // active / inactive / maintenance / ...
    val moderationStatus: ModerationStatusEnum?,
    val status: UploadStatusEnum,

    val adminComment: String?,  // комментарий модерации

    // Серверные временны́е метки (заполняются после синхронизации)
    val createdAt: Long?,
    val updatedAt: Long?,

    // Технические поля
    val localCreatedAt: Long,

    // Поле для отслеживания локальных изменений
    // может и не надо, подумать
    val lastUpdatedLocally: Long,

    // Список clientId присоединенных фотографий для создании кросс связей.
    // переделать под другой тип, что было безопасно
    // можно потом будет создать wrapper для кросссвязей

    /*
    data class AssetWithPhotos(
    val asset: Asset,
    val photos: List<Photo>
      )
     */

    val photoClientIds: List<String> = emptyList()

) {
    // Бизнес-свойство: синхронизирован ли актив?
    val isSyncedToServer: Boolean get() = serverUuid != null

    // Бизнес-свойство: можем ли мы работать с этим активом через API?
    val isRemoteReady: Boolean get() = isSyncedToServer

}
