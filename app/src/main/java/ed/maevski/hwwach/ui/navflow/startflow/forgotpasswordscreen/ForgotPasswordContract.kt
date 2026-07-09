package ed.maevski.hwwach.ui.navflow.startflow.forgotpasswordscreen

sealed class ForgotPasswordAction {
    data class InputLogin(val login: String) : ForgotPasswordAction()
    data object OnSubmitClicked : ForgotPasswordAction()
    data object OnBackClicked : ForgotPasswordAction()
}

sealed class ForgotPasswordEvent {
    data object Exit : ForgotPasswordEvent()
}

data class ForgotPasswordState(
    val login: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
