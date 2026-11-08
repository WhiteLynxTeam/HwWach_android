package ed.maevski.hwwach.common.deeplink

import android.net.Uri
import ed.maevski.hwwach.domain.models.OrgNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Номер организации из ссылки-приглашения hwwach://login?org=4829131 (страница /org/{номер} на сервере,
 * её открывает QR администратора). Экран входа подставляет номер в поле.
 */
object PendingOrgNumber {
    private val _number = MutableStateFlow<String?>(null)
    val number: StateFlow<String?> = _number.asStateFlow()

    fun handle(uri: Uri?) {
        if (uri == null || uri.scheme != "hwwach" || uri.host != "login") return
        val digits = OrgNumber.digits(uri.getQueryParameter("org").orEmpty())
        if (OrgNumber.isValid(digits)) _number.value = digits
    }

    // забрать номер один раз: при повороте экрана повторно не применяем
    fun consume(): String? {
        val value = _number.value
        _number.value = null
        return value
    }
}
