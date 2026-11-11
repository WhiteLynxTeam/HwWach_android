package ed.maevski.hwwach.domain.istorage

interface IFileStorage {
    /** Читает файл по URI (content:// или file://) в байтовый массив */
    fun readBytes(uri: String): ByteArray?

    /**
     * Готовит фото к хранению и загрузке: уменьшает до 2048 px по длинной стороне, поворачивает по EXIF,
     * пересжимает в JPEG 85 и кладёт в постоянную папку приложения (filesDir/photos), возвращает file:// URI.
     * Временный снимок камеры после этого удаляется. Вызывать не в главном потоке.
     */
    fun importPhoto(uri: String): String

    /** Удаляет файл по URI */
    fun deleteFile(uri: String)

    /** Проверяет существование файла по URI (file://) */
    fun fileExists(uri: String): Boolean

    companion object {
        /** Папка внутри cacheDir для временных снимков камеры — [importPhoto] удаляет их после обработки */
        const val CAMERA_TEMP_DIR = "camera"
    }
}
