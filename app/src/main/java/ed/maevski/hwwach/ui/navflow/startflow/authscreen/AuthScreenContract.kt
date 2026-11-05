package ed.maevski.hwwach.ui.navflow.startflow.authscreen

import ed.maevski.hwwach.domain.models.OrgNumber
import ed.maevski.hwwach.domain.models.RegStatusEnum

sealed class AuthScreenAction {
    data class InputLogin(val login: String) : AuthScreenAction()
    data class InputPassword(val password: String) : AuthScreenAction()
    data class InputOrgNumber(val number: String) : AuthScreenAction()
    /** «Сменить» у запомненной организации — снова показать поле номера. */
    data object OnChangeOrgClicked : AuthScreenAction()
    data object OnAuthClicked : AuthScreenAction()
    data object OnRegClicked : AuthScreenAction()
    data object OnOrgRegClicked : AuthScreenAction()
    data object OnForgotPasswordClicked : AuthScreenAction()
    data object OnBackClicked : AuthScreenAction()
    data class OnPrivacyPolicyChecked(val checked: Boolean) : AuthScreenAction()
    data object OnPrivacyPolicyClicked : AuthScreenAction()
}

sealed class AuthScreenEvent {
    data object NavigateToMain : AuthScreenEvent()
    data object NavigateToReg : AuthScreenEvent()
    data object NavigateToOrgReg : AuthScreenEvent()
    data object NavigateToForgotPassword : AuthScreenEvent()
    data class NavigateToChangeTempPassword(val login: String, val tempPassword: String, val organizationNumber: String?) : AuthScreenEvent()
    data object NavigateToPrivacyPolicy : AuthScreenEvent()
    data object Exit : AuthScreenEvent()
}

data class AuthScreenState (
    val login: String = "",
    val password: String = "",
    // Номер организации (9.2): цифры, как ввёл человек; пусто — вход по почте или однозначному логину
    val orgNumber: String = "",
    // Организация последнего входа: номер уже есть, вместо поля — её название и «Сменить»
    val rememberedOrgName: String? = null,
    // Логин есть в нескольких организациях — без номера не войти
    val isOrgNumberRequired: Boolean = false,
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val registrationStatusMessage: String? = null,
    val registrationStatus: RegStatusEnum? = null,
    val isPrivacyPolicyAccepted: Boolean = false,
) {
    /** Номер набран полностью, но контрольная цифра не сходится — опечатка. */
    val isOrgNumberInvalid: Boolean
        get() = OrgNumber.digits(orgNumber).length == OrgNumber.LENGTH &&
            !OrgNumber.isValid(orgNumber)
}
