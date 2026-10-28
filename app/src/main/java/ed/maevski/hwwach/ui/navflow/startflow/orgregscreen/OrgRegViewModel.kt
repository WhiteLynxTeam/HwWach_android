package ed.maevski.hwwach.ui.navflow.startflow.orgregscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrgRegViewModel @Inject constructor(
    private val orgRepository: IOrgRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrgRegState())
    val state: StateFlow<OrgRegState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<OrgRegEvent>()
    val events: SharedFlow<OrgRegEvent> = _events.asSharedFlow()

    fun handleAction(action: OrgRegAction) {
        when (action) {
            is OrgRegAction.Input -> onInput(action.field, action.value)
            OrgRegAction.OnNextClicked -> onNext()
            OrgRegAction.OnResendClicked -> sendCode()
            OrgRegAction.OnBackClicked -> onBack()
            // на экран регистрации организации попадают со входа — возврат к нему
            OrgRegAction.OnAuthClicked -> viewModelScope.launch { _events.emit(OrgRegEvent.Exit) }
        }
    }

    private fun onInput(field: OrgRegField, value: String) {
        _state.update {
            when (field) {
                OrgRegField.EMAIL -> it.copy(email = value.trim())
                OrgRegField.CODE -> it.copy(code = value.filter { c -> c.isDigit() }.take(6))
                OrgRegField.NAME -> it.copy(name = value)
                OrgRegField.SHORT_NAME -> it.copy(shortName = value)
                OrgRegField.ADDRESS -> it.copy(address = value)
                OrgRegField.DIRECTOR -> it.copy(directorName = value)
                OrgRegField.OWNER -> it.copy(ownerName = value)
                OrgRegField.LAST_NAME -> it.copy(lastName = value)
                OrgRegField.FIRST_NAME -> it.copy(firstName = value)
                OrgRegField.MIDDLE_NAME -> it.copy(middleName = value)
                OrgRegField.POSITION -> it.copy(position = value)
                OrgRegField.PASSWORD -> it.copy(password = value)
            }.copy(errorMessage = "")
        }
    }

    private fun onBack() {
        val step = _state.value.step
        if (step == OrgRegStep.EMAIL) {
            viewModelScope.launch { _events.emit(OrgRegEvent.Exit) }
        } else {
            _state.update { it.copy(step = if (step == OrgRegStep.DETAILS) OrgRegStep.CODE else OrgRegStep.EMAIL, errorMessage = "") }
        }
    }

    private fun onNext() {
        when (_state.value.step) {
            OrgRegStep.EMAIL -> sendCode()
            OrgRegStep.CODE -> verifyCode()
            OrgRegStep.DETAILS -> complete()
        }
    }

    private fun errorText(result: DomainResult<*>): String = when (result) {
        is DomainResult.ValidationError -> result.message
        is DomainResult.NetworkError -> "Нет связи с сервером. Подождите минуту и попробуйте снова"
        else -> "Ошибка сервера, попробуйте позже"
    }

    // Шаг 1: письмо с кодом. Ответ одинаковый, даже если на почту уже есть организация — тогда в письме подсказка.
    // Название на сервер уходит на шаге 3 вместе с остальными данными
    private fun sendCode() {
        val email = _state.value.email
        if (_state.value.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Введите название организации") }
            return
        }
        if (!email.contains("@")) {
            _state.update { it.copy(errorMessage = "Введите адрес почты") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = "") }
            when (val result = orgRepository.sendOrgCode(email)) {
                is DomainResult.Success -> _state.update {
                    it.copy(isLoading = false, step = OrgRegStep.CODE, infoMessage = "Мы отправили письмо на $email. Введите код из письма")
                }
                else -> _state.update { it.copy(isLoading = false, errorMessage = errorText(result)) }
            }
        }
    }

    private fun verifyCode() {
        val s = _state.value
        if (s.code.length != 6) {
            _state.update { it.copy(errorMessage = "Код — 6 цифр") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = "") }
            when (val result = orgRepository.verifyOrgCode(s.email, s.code)) {
                is DomainResult.Success -> _state.update {
                    it.copy(isLoading = false, registrationToken = result.data, step = OrgRegStep.DETAILS, infoMessage = "")
                }
                else -> _state.update { it.copy(isLoading = false, errorMessage = errorText(result)) }
            }
        }
    }

    // Шаг 3: организация + администратор (логин — эта почта) → сразу вход
    private fun complete() {
        val s = _state.value
        val token = s.registrationToken ?: return
        if (s.name.isBlank()) {
            _state.update { it.copy(step = OrgRegStep.EMAIL, errorMessage = "Введите название организации") }
            return
        }
        if (s.lastName.isBlank() || s.firstName.isBlank()) {
            _state.update { it.copy(errorMessage = "Укажите фамилию и имя") }
            return
        }
        if (s.password.length < 4) {
            _state.update { it.copy(errorMessage = "Пароль — не меньше 4 символов") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = "") }
            val result = orgRepository.completeOrgRegistration(
                registrationToken = token,
                name = s.name,
                shortName = s.shortName,
                address = s.address,
                directorName = s.directorName,
                ownerName = s.ownerName,
                lastName = s.lastName,
                firstName = s.firstName,
                middleName = s.middleName,
                position = s.position,
                password = s.password,
            )
            when (result) {
                is DomainResult.Success -> _events.emit(OrgRegEvent.NavigateToMain)
                is DomainResult.UnauthorizedError -> _state.update {
                    it.copy(isLoading = false, step = OrgRegStep.EMAIL, errorMessage = "Срок подтверждения почты истёк. Начните заново")
                }
                else -> _state.update { it.copy(isLoading = false, errorMessage = errorText(result)) }
            }
        }
    }
}
