package ed.maevski.hwwach.common.deeplink

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Код приглашения из ссылки hwwach://invite?code=K7M-42Q (страница приглашения на сервере).
 * Экран входа видит код и открывает регистрацию, регистрация подставляет его в поле.
 */
object PendingInvite {
    private val _code = MutableStateFlow<String?>(null)
    val code: StateFlow<String?> = _code.asStateFlow()

    fun handle(uri: Uri?) {
        if (uri == null || uri.scheme != "hwwach" || uri.host != "invite") return
        uri.getQueryParameter("code")?.takeIf { it.isNotBlank() }?.let { _code.value = it.trim() }
    }

    // забрать код один раз: при повороте экрана повторно не применяем
    fun consume(): String? {
        val value = _code.value
        _code.value = null
        return value
    }

    fun peek(): String? = _code.value
}
