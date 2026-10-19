package ed.maevski.hwwach.ui.navflow.adminflow

import ed.maevski.hwwach.domain.DomainResult

/** Текст ошибки для админских экранов; null — если результат успешный */
fun DomainResult<*>.toAdminErrorMessage(): String? = when (this) {
    is DomainResult.Success -> null
    is DomainResult.UnauthorizedError -> "Сессия устарела, войдите заново"
    is DomainResult.ValidationError -> message
    is DomainResult.ServerError -> "Ошибка сервера: код $code"
    is DomainResult.NetworkError -> "Ошибка сети: $message"
}
