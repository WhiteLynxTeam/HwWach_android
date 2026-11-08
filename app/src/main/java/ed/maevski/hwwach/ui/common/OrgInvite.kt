package ed.maevski.hwwach.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import ed.maevski.hwwach.BuildConfig
import ed.maevski.hwwach.domain.models.OrgNumber

/** Ссылка-приглашение для входа: страница сервера с номером, кнопкой «Открыть в приложении» и скачиванием. */
object OrgInvite {
    fun link(number: String): String = BuildConfig.BASE_AUTH_URL.trimEnd('/') + "/org/" + number

    fun shareText(number: String, organizationName: String?): String = buildString {
        append("Приложение HwWach")
        organizationName?.takeIf { it.isNotBlank() }?.let { append(" — «").append(it).append("»") }
        append(".\nНомер организации: ").append(OrgNumber.format(number))
        append("\nСсылка: ").append(link(number))
    }

    fun copy(context: Context, number: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("Номер организации", number))
        Toast.makeText(context, "Номер скопирован", Toast.LENGTH_SHORT).show()
    }

    fun share(context: Context, number: String, organizationName: String?) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, shareText(number, organizationName))
        context.startActivity(Intent.createChooser(send, "Пригласить сотрудника"))
    }

    /** QR со ссылкой: камера телефона откроет страницу, а с неё — приложение. Чёрный по белому, с полем по краю. */
    fun qr(number: String, size: Int = 720): Bitmap {
        val matrix = QRCodeWriter().encode(
            link(number), BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 2),
        )
        val pixels = IntArray(size * size) { i ->
            if (matrix.get(i % size, i / size)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}
