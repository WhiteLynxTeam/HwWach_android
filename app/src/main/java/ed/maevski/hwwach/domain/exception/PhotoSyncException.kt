package ed.maevski.hwwach.domain.exception

/**
 * Исключение при ошибке синхронизации фото.
 * Бросается из syncPhotosByClientIds, если хотя бы одно фото не удалось загрузить.
 */
class PhotoSyncException(
    message: String,
    failedPhotoClientId: String? = null
) : SyncException(message, failedPhotoClientId, SyncEntityType.PHOTO)