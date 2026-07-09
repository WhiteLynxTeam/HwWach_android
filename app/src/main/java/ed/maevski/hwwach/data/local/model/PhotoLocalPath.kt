package ed.maevski.hwwach.data.local.model

/**
 * Проекция для частичного выбора полей из таблицы photos.
 * Используется в JOIN-оптимизации при синхронизации.
 *
 * see PhotoDao.getExistingLocalPaths
 */
class PhotoLocalPath(
    val clientId: String,
    val localFilePath: String?
)
