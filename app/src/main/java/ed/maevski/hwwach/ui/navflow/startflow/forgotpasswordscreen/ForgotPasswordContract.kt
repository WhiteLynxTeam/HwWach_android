package ed.maevski.hwwach.ui.navflow.startflow.forgotpasswordscreen

import ed.maevski.hwwach.domain.models.OrgNumber

sealed class ForgotPasswordAction {
    data class InputLogin(val login: String) : ForgotPasswordAction()
    data class InputOrgNumber(val number: String) : ForgotPasswordAction()
    /** «Сменить» у запомненной организации — снова показать поле номера. */
    data object OnChangeOrgClicked : ForgotPasswordAction()
    data object OnSubmitClicked : ForgotPasswordAction()
    data object OnBackClicked : ForgotPasswordAction()
}

sealed class ForgotPasswordEvent {
    data object Exit : ForgotPasswordEvent()
}

data class ForgotPasswordState(
    val login: String = "",
    // Номер организации (9.2): как на экране входа — логин уникален только внутри организации
    val orgNumber: String = "",
    val rememberedOrgName: String? = null,
    val isOrgNumberRequired: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    /** Номер набран полностью, но контрольная цифра не сходится — опечатка. */
    val isOrgNumberInvalid: Boolean
        get() = OrgNumber.digits(orgNumber).length == OrgNumber.LENGTH &&
            !OrgNumber.isValid(orgNumber)
}
