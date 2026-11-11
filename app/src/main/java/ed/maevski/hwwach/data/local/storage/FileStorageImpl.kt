package ed.maevski.hwwach.data.local.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import ed.maevski.hwwach.domain.istorage.IFileStorage
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max

class FileStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IFileStorage {

    override fun readBytes(uri: String): ByteArray? = try {
        //[AI change to] String.toUri
        val androidUri = uri.toUri()
        context.contentResolver.openInputStream(androidUri)?.use { it.readBytes() }
    } catch (_: Exception) {
        null
    }

    override fun importPhoto(uri: String): String {
        val source = uri.toUri()
        val bitmap = decodeForUpload(source)
            ?: throw IllegalStateException("Не удалось прочитать изображение $uri")

        // filesDir, а не cacheDir: система не очистит фото, которое ещё ждёт загрузки
        val dir = File(context.filesDir, PHOTOS_DIR).apply { if (!exists()) mkdirs() }
        val outFile = File(dir, "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg")
        try {
            outFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        } catch (e: Exception) {
            outFile.delete()
            throw e
        } finally {
            bitmap.recycle()
        }

        deleteCameraTemp(source)
        return Uri.fromFile(outFile).toString()
    }

    override fun deleteFile(uri: String) {
        val parsedUri = uri.toUri()
        if (parsedUri.scheme == "file") {
            val file = File(parsedUri.path ?: return)
            if (file.exists()) file.delete()
        }
    }

    override fun fileExists(uri: String): Boolean {
        return try {
            val parsedUri = uri.toUri()
            val path = if (parsedUri.scheme == "file") parsedUri.path else uri
            path != null && File(path).exists()
        } catch (_: Exception) {
            false
        }
    }

    private fun open(uri: Uri): InputStream? = context.contentResolver.openInputStream(uri)

    /**
     * Декодирует с уменьшением: сначала грубо через inSampleSize (степень двойки — экономит память
     * на снимках 50 Мп), затем точно до [MAX_SIDE] одной матрицей вместе с поворотом по EXIF.
     */
    private fun decodeForUpload(source: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        (open(source) ?: return null).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = (open(source) ?: return null).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val matrix = Matrix()
        val scale = MAX_SIDE.toFloat() / max(decoded.width, decoded.height)
        if (scale < 1f) matrix.postScale(scale, scale)
        applyExifOrientation(matrix, readOrientation(source))
        if (matrix.isIdentity) return decoded

        val result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (result !== decoded) decoded.recycle()
        return result
    }

    private fun readOrientation(source: Uri): Int = try {
        open(source)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (e: Exception) {
        Log.w(TAG, "EXIF не прочитан, фото без поворота", e)
        ExifInterface.ORIENTATION_NORMAL
    }

    // CameraX и большинство камер не поворачивают пиксели, а пишут ориентацию в EXIF.
    // При пересжатии EXIF теряется, поэтому поворот нужно применить к самому изображению.
    private fun applyExifOrientation(matrix: Matrix, orientation: Int) {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> { matrix.postRotate(180f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(-90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(-90f)
        }
    }

    /**
     * Удаляет исходник, если это временный снимок камеры (cacheDir/camera). Файлы галереи (content://)
     * не трогаем. Заодно убираем снимки прежних версий, которые копились в externalCacheDir.
     */
    private fun deleteCameraTemp(source: Uri) {
        try {
            if (source.scheme == "file") {
                val file = File(source.path ?: return).canonicalFile
                val cameraDir = File(context.cacheDir, IFileStorage.CAMERA_TEMP_DIR).canonicalFile
                if (file.parentFile == cameraDir) file.delete()
            }
            context.externalCacheDir
                ?.listFiles { f -> f.isFile && f.name.startsWith(LEGACY_CAMERA_PREFIX) && f.name.endsWith(".jpg") }
                ?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.w(TAG, "Временный снимок не удалён", e)
        }
    }

    private companion object {
        const val TAG = "FileStorage"
        const val PHOTOS_DIR = "photos"
        const val MAX_SIDE = 2048
        const val JPEG_QUALITY = 85
        const val LEGACY_CAMERA_PREFIX = "HW_watch_"
    }
}
