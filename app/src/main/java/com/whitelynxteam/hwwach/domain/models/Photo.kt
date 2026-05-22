package com.whitelynxteam.hwwach.domain.models

data class Photo(
    val clientId: String,
    val serverUuid: String?,

    val localCreatedAt: Long,
    val status: UploadStatusEnum,

    val localPath: String?,
    val remoteUrl: String?,

    val errorMessage: String? = null
) {
    // Бизнес-свойство: синхронизировано ли фото?
    val isSyncedToServer: Boolean get() = serverUuid != null

    // Бизнес-свойство: можем ли мы работать с этим фото через API?
    val isRemoteReady: Boolean get() = isSyncedToServer && remoteUrl != null
}
