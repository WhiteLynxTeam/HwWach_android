package ed.maevski.hwwach.ui.navflow.startflow.authscreen

import ed.maevski.hwwach.domain.models.RegStatusEnum

sealed class AuthScreenAction {
    data class InputLogin(val login: String) : AuthScreenAction()
    data class InputPassword(val password: String) : AuthScreenAction()
    data object OnAuthClicked : AuthScreenAction()
    data object OnRegClicked : AuthScreenAction()
    data object OnForgotPasswordClicked : AuthScreenAction()
    data object OnBackClicked : AuthScreenAction()
    data class OnPrivacyPolicyChecked(val checked: Boolean) : AuthScreenAction()
    data object OnPrivacyPolicyClicked : AuthScreenAction()
}

sealed class AuthScreenEvent {
    data object NavigateToMain : AuthScreenEvent()
    data object NavigateToReg : AuthScreenEvent()
    data object NavigateToForgotPassword : AuthScreenEvent()
    data class NavigateToChangeTempPassword(val login: String, val tempPassword: String) : AuthScreenEvent()
    data object NavigateToPrivacyPolicy : AuthScreenEvent()
    data object Exit : AuthScreenEvent()
}

data class AuthScreenState (
    val login: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val registrationStatusMessage: String? = null,
    val registrationStatus: RegStatusEnum? = null,
    val isPrivacyPolicyAccepted: Boolean = false,
)