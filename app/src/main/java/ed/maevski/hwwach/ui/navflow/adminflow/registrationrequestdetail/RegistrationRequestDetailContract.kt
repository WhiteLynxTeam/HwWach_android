package ed.maevski.hwwach.ui.navflow.adminflow.registrationrequestdetail

import ed.maevski.hwwach.domain.models.RegistrationRequest

sealed class RegistrationRequestDetailAction {
    data object OnRetry : RegistrationRequestDetailAction()
    data object OnApproveClicked : RegistrationRequestDetailAction()
    data object OnApproveConfirmed : RegistrationRequestDetailAction()
    data object OnRejectClicked : RegistrationRequestDetailAction()
    data class OnRejectReasonChanged(val reason: String) : RegistrationRequestDetailAction()
    data object OnRejectConfirmed : RegistrationRequestDetailAction()
    data object OnDialogDismissed : RegistrationRequestDetailAction()
    data object OnBackPressed : RegistrationRequestDetailAction()
}

sealed class RegistrationRequestDetailEvent {
    data class ShowMessage(val message: String) : RegistrationRequestDetailEvent()
    data object NavigateBack : RegistrationRequestDetailEvent()
}

enum class RegistrationRequestDialog { APPROVE, REJECT }

data class RegistrationRequestDetailState(
    val request: RegistrationRequest? = null,
    val isLoading: Boolean = true,
    val loadErrorMessage: String? = null,
    // идёт запрос одобрения/отклонения — кнопки заблокированы
    val isProcessing: Boolean = false,
    val dialog: RegistrationRequestDialog? = null,
    val rejectReason: String = ""
)
