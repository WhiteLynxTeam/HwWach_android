package ed.maevski.hwwach.ui.navflow.startflow.forgotpasswordscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.models.OrgNumber
import ed.maevski.hwwach.domain.usecases.user.RequestPasswordResetUseCase
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

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val orgRepository: IOrgRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

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
            ForgotPasswordAction.OnSubmitClicked -> onSubmitClicked()
            ForgotPasswordAction.OnBackClicked -> onBackClicked()
        }
    }

    private fun onSubmitClicked() {
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

            // Заявка уходит админу организации: он задаёт временный пароль и сообщает его,
            // при входе с ним приложение попросит придумать новый. Старый пароль пока работает
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
                            errorMessage = "Такой логин есть в нескольких организациях.\nВведите номер организации или войдите по почте"
                        )
                    } else {
                        it.copy(isLoading = false, errorMessage = "Ошибка сервера: ${result.code}")
                    }
                }
                // 404 — логин не найден, 409 — заявка уже на рассмотрении: текст от сервера
                is DomainResult.ValidationError -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                is DomainResult.NetworkError -> _state.update {
                    it.copy(isLoading = false, errorMessage = "Нет связи с сервером. Попробуйте ещё раз")
                }
                DomainResult.UnauthorizedError -> _state.update {
                    it.copy(isLoading = false, errorMessage = "Ошибка сервера")
                }
            }
        }
    }

    private fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(ForgotPasswordEvent.Exit)
        }
    }
}
