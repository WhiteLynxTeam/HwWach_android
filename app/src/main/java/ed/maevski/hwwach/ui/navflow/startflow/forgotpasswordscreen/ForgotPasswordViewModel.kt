package ed.maevski.hwwach.ui.navflow.startflow.forgotpasswordscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.models.OrgNumber
import ed.maevski.hwwach.domain.usecases.user.PasswordResetByEmailUseCase
import ed.maevski.hwwach.domain.usecases.user.RequestPasswordResetUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * «Забыли пароль?»:
 * - почта → код на неё (8.2) → новый пароль, без админа; так восстанавливает пароль и сам админ;
 * - логин → заявка админу организации, он задаёт временный пароль.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val passwordResetByEmailUseCase: PasswordResetByEmailUseCase,
    private val orgRepository: IOrgRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    private var resendTimer: Job? = null

    init {
        // Организация последнего входа (9.2) — как на экране входа
        viewModelScope.launch {
            orgRepository.lastOrganization.first()?.let { (number, name) ->
                _state.update { it.copy(orgNumber = number, rememberedOrgName = name.ifBlank { OrgNumber.format(number) }) }
            }
        }
    }

    fun handleAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.InputLogin -> {
                _state.update { it.copy(login = action.login, errorMessage = null, successMessage = null) }
            }
            is ForgotPasswordAction.InputOrgNumber -> {
                _state.update { it.copy(orgNumber = OrgNumber.digits(action.number), errorMessage = null, successMessage = null) }
            }
            ForgotPasswordAction.OnChangeOrgClicked -> {
                _state.update { it.copy(orgNumber = "", rememberedOrgName = null) }
            }
            ForgotPasswordAction.OnSubmitClicked -> {
                if (_state.value.isEmail) sendCode(_state.value.login.trim()) else requestReset()
            }
            is ForgotPasswordAction.InputCode -> {
                _state.update { it.copy(code = action.code.filter(Char::isDigit).take(CODE_LENGTH), errorMessage = null) }
            }
            is ForgotPasswordAction.InputNewPassword -> {
                _state.update { it.copy(newPassword = action.password, errorMessage = null) }
            }
            is ForgotPasswordAction.InputRepeatPassword -> {
                _state.update { it.copy(repeatPassword = action.password, errorMessage = null) }
            }
            ForgotPasswordAction.OnConfirmClicked -> confirm()
            ForgotPasswordAction.OnResendClicked -> sendCode(_state.value.email)
            ForgotPasswordAction.OnChangeEmailClicked -> {
                resendTimer?.cancel()
                _state.update {
                    it.copy(step = ForgotPasswordStep.INPUT, code = "", newPassword = "", repeatPassword = "",
                        resendInSec = 0, errorMessage = null, successMessage = null)
                }
            }
            ForgotPasswordAction.OnBackClicked -> onBackClicked()
        }
    }

    // Заявка админу по логину
    private fun requestReset() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.login.isBlank()) {
                _state.update { it.copy(errorMessage = "Введите логин или почту") }
                return@launch
            }

            // Номер набран с опечаткой — контрольная цифра не сходится, на сервер не идём
            val orgNumber = currentState.orgNumber.takeIf { it.isNotBlank() }
            if (orgNumber != null && !OrgNumber.isValid(orgNumber)) {
                _state.update { it.copy(errorMessage = "Номер организации неверный — проверьте цифры") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            // Админ задаёт временный пароль и сообщает его, при входе с ним приложение
            // попросит придумать новый. Старый пароль пока работает
            when (val result = requestPasswordResetUseCase(currentState.login.trim(), orgNumber)) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Заявка отправлена администратору организации.\n" +
                            "Когда он её одобрит, он сообщит вам временный пароль — при входе его нужно будет сменить."
                    )
                }
                is DomainResult.ServerError -> _state.update {
                    if (result.code == 409) {
                        // Такой логин есть в нескольких организациях (9.2)
                        it.copy(
                            isLoading = false,
                            isOrgNumberRequired = true,
                            rememberedOrgName = null,
                            errorMessage = "Такой логин есть в нескольких организациях.\nВведите номер организации или почту вместо логина"
                        )
                    } else {
                        it.copy(isLoading = false, errorMessage = "Ошибка сервера: ${result.code}")
                    }
                }
                else -> showError(result)
            }
        }
    }

    // Код на почту. Ответ сервера одинаковый, есть адрес или нет: если учётной записи нет
    // или почта не подтверждена — в письме написано, что делать
    private fun sendCode(email: String) {
        viewModelScope.launch {
            if (!EMAIL.matches(email)) {
                _state.update { it.copy(errorMessage = "Проверьте адрес почты") }
                return@launch
            }
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = passwordResetByEmailUseCase.sendCode(email)) {
                is DomainResult.Success -> {
                    _state.update {
                        it.copy(
                            step = ForgotPasswordStep.CODE,
                            email = email,
                            code = "",
                            isLoading = false,
                            successMessage = "Если адрес $email есть в HwWach, мы отправили на него код.\n" +
                                "Письма нет — проверьте «Спам»."
                        )
                    }
                    startResendTimer()
                }
                else -> showError(result)
            }
        }
    }

    private fun confirm() {
        viewModelScope.launch {
            val s = _state.value
            when {
                s.code.length != CODE_LENGTH -> {
                    _state.update { it.copy(errorMessage = "Введите 6 цифр из письма") }
                    return@launch
                }
                s.newPassword.length < MIN_PASSWORD -> {
                    _state.update { it.copy(errorMessage = "Пароль — не короче $MIN_PASSWORD символов") }
                    return@launch
                }
                s.newPassword != s.repeatPassword -> {
                    _state.update { it.copy(errorMessage = "Пароли не совпадают") }
                    return@launch
                }
            }
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = passwordResetByEmailUseCase.confirm(s.email, s.code, s.newPassword)) {
                is DomainResult.Success -> {
                    resendTimer?.cancel()
                    val org = result.data.organizationName?.takeIf { it.isNotBlank() }
                    _state.update {
                        it.copy(
                            step = ForgotPasswordStep.DONE,
                            isLoading = false,
                            code = "",
                            newPassword = "",
                            repeatPassword = "",
                            successMessage = "Пароль изменён. Войдите с новым паролем.\n" +
                                "Логин: ${result.data.login}" + (org?.let { o -> "\nОрганизация: $o" } ?: "") +
                                "\nНа других устройствах нужно будет войти заново."
                        )
                    }
                }
                // Неверный код — сервер пишет, сколько попыток осталось
                else -> showError(result)
            }
        }
    }

    private fun showError(result: DomainResult<*>) {
        val message = when (result) {
            is DomainResult.ValidationError -> result.message
            is DomainResult.NetworkError -> "Нет связи с сервером. Попробуйте ещё раз"
            is DomainResult.ServerError -> "Ошибка сервера: ${result.code}"
            else -> "Ошибка сервера"
        }
        _state.update { it.copy(isLoading = false, errorMessage = message) }
    }

    private fun startResendTimer() {
        resendTimer?.cancel()
        resendTimer = viewModelScope.launch {
            for (sec in RESEND_SEC downTo 0) {
                _state.update { it.copy(resendInSec = sec) }
                if (sec > 0) delay(1000)
            }
        }
    }

    private fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(ForgotPasswordEvent.Exit)
        }
    }

    private companion object {
        const val CODE_LENGTH = 6
        const val MIN_PASSWORD = 4
        const val RESEND_SEC = 60
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
