package ed.maevski.hwwach.ui.navflow.adminflow.passwordresets

import ed.maevski.hwwach.domain.models.PasswordResetRequest

sealed class PasswordResetsAction {
    data object OnScreenShown : PasswordResetsAction()
    data object OnRefresh : PasswordResetsAction()
    data object OnBackPressed : PasswordResetsAction()
    data class OnResetClicked(val request: PasswordResetRequest) : PasswordResetsAction()
    data class OnRejectClicked(val request: PasswordResetRequest) : PasswordResetsAction()
    data class InputPassword(val value: String) : PasswordResetsAction()
    data object OnConfirmReset : PasswordResetsAction()
    data object OnDismissDialog : PasswordResetsAction()
}

sealed class PasswordResetsEvent {
    data class ShowMessage(val message: String) : PasswordResetsEvent()
    data object NavigateBack : PasswordResetsEvent()
}

/** Окно с временным паролем после сброса — админ передаёт его пользователю */
data class PasswordResetDone(
    val login: String,
    val tempPassword: String?,
    val isDefault: Boolean
)

data class PasswordResetsState(
    val requests: List<PasswordResetRequest> = emptyList(),
    // первичная загрузка — спиннер на весь экран
    val isLoading: Boolean = true,
    // pull-to-refresh — индикатор сверху, список остаётся
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    // заявка, для которой открыт диалог сброса
    val resetTarget: PasswordResetRequest? = null,
    val passwordInput: String = "",
    val dialogError: String? = null,
    // идёт запрос сброса или отклонения
    val processingId: String? = null,
    val done: PasswordResetDone? = null
)
