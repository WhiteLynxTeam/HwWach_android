package ed.maevski.hwwach.data.local.model

/**
 * Проекция для частичного выбора полей из таблицы photos.
 * Используется в JOIN-оптимизации при синхронизации: сервер не знает локальный путь,
 * группу и подпись — их сохраняем при замене записи.
 *
 * see PhotoDao.getExistingLocalPaths
 */
class PhotoLocalPath(
    val clientId: String,
    val localFilePath: String?,
    val groupId: String?,
    val note: String?,
)
