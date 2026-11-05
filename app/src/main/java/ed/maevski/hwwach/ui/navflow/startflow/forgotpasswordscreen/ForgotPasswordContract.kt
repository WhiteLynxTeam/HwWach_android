package ed.maevski.hwwach.ui.navflow.startflow.forgotpasswordscreen

import ed.maevski.hwwach.domain.models.OrgNumber

sealed class ForgotPasswordAction {
    data class InputLogin(val login: String) : ForgotPasswordAction()
    data class InputOrgNumber(val number: String) : ForgotPasswordAction()
    /** «Сменить» у запомненной организации — снова показать поле номера. */
    data object OnChangeOrgClicked : ForgotPasswordAction()
    data object OnSubmitClicked : ForgotPasswordAction()
    // Шаг «код из письма»
    data class InputCode(val code: String) : ForgotPasswordAction()
    data class InputNewPassword(val password: String) : ForgotPasswordAction()
    data class InputRepeatPassword(val password: String) : ForgotPasswordAction()
    data object OnConfirmClicked : ForgotPasswordAction()
    data object OnResendClicked : ForgotPasswordAction()
    /** «Ввести другой адрес» — назад к первому шагу. */
    data object OnChangeEmailClicked : ForgotPasswordAction()
    data object OnBackClicked : ForgotPasswordAction()
}

sealed class ForgotPasswordEvent {
    data object Exit : ForgotPasswordEvent()
}

enum class ForgotPasswordStep {
    /** Логин или почта (+ номер организации для логина). */
    INPUT,
    /** Почта: код из письма и новый пароль. */
    CODE,
    /** Пароль сменён по коду — можно входить. */
    DONE,
}

data class ForgotPasswordState(
    val step: ForgotPasswordStep = ForgotPasswordStep.INPUT,
    val login: String = "",
    // Номер организации (9.2): как на экране входа — логин уникален только внутри организации
    val orgNumber: String = "",
    val rememberedOrgName: String? = null,
    val isOrgNumberRequired: Boolean = false,
    // Шаг CODE: адрес, на который ушёл код
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
    // Повторно отправить код можно через минуту (лимит сервера)
    val resendInSec: Int = 0,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    /** Введена почта — сброс кодом на неё; иначе логин — заявка админу. */
    val isEmail: Boolean
        get() = login.contains('@')

    /** Номер набран полностью, но контрольная цифра не сходится — опечатка. */
    val isOrgNumberInvalid: Boolean
        get() = OrgNumber.digits(orgNumber).length == OrgNumber.LENGTH &&
            !OrgNumber.isValid(orgNumber)
}
