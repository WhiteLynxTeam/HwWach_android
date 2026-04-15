package com.whitelynxteam.hwwach.domain.istorage

interface IFileStorage {
    /** Читает файл по URI (content:// или file://) в байтовый массив */
    fun readBytes(uri: String): ByteArray?

    /** Копирует content:// URI в cacheDir, возвращает file:// URI */
    fun copyToCache(uri: String): String
}
