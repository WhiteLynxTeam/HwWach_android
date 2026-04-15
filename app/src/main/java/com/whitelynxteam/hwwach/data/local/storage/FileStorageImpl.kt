package com.whitelynxteam.hwwach.data.local.storage

import android.content.Context
import android.net.Uri
import com.whitelynxteam.hwwach.domain.istorage.IFileStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class FileStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IFileStorage {

    override fun readBytes(uri: String): ByteArray? = try {
        //[AI change to] String.toUri
        val androidUri = Uri.parse(uri)
        context.contentResolver.openInputStream(androidUri)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }

    override fun copyToCache(uri: String): String {
        //[AI change to] String.toUri
        val contentUri = Uri.parse(uri)
        if (!contentUri.scheme.equals("content", ignoreCase = true)) return uri

        val inputStream = context.contentResolver.openInputStream(contentUri)
            ?: throw IllegalStateException("Cannot open InputStream for $uri")
        val dir = File(context.cacheDir, "photos").apply { if (!exists()) mkdirs() }
        val outFile = File(dir, "${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(outFile).toString()
    }
}
