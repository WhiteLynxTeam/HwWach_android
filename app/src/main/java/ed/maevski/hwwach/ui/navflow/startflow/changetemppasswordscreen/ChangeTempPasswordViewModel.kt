package ed.maevski.hwwach.ui.navflow.startflow.changetemppasswordscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.usecases.user.ChangeTempPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ChangeTempPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val changeTempPasswordUseCase: ChangeTempPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChangeTempPasswordState())
    val state: StateFlow<ChangeTempPasswordState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChangeTempPasswordEvent>()
    val events: SharedFlow<ChangeTempPasswordEvent> = _events.asSharedFlow()

    init {
        val loginArg = savedStateHandle.get<String>("login") ?: ""
        val tempPasswordArg = savedStateHandle.get<String>("tempPassword") ?: ""
        _state.update {
            it.copy(login = loginArg, oldPassword = tempPasswordArg)
        }
    }

    fun handleAction(action: ChangeTempPasswordAction) {
        when (action) {
            is ChangeTempPasswordAction.InputLogin -> {
                _state.update { it.copy(login = action.login, errorMessage = null, successMessage = null) }
            }
            is ChangeTempPasswordAction.InputOldPassword -> {
                _state.update { it.copy(oldPassword = action.oldPass, errorMessage = null, successMessage = null) }
            }
            is ChangeTempPasswordAction.InputNewPassword -> {
                _state.update { it.copy(newPassword = action.newPass, errorMessage = null, successMessage = null) }
            }
            ChangeTempPasswordAction.OnSubmitClicked -> onSubmitClicked()
            ChangeTempPasswordAction.OnBackClicked -> onBackClicked()
        }
    }

    private fun onSubmitClicked() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.login.isBlank() || currentState.oldPassword.isBlank() || currentState.newPassword.isBlank()) {
                _state.update { it.copy(errorMessage = "Заполните все поля") }
                return@launch
            }

            if (currentState.oldPassword == currentState.newPassword) {
                _state.update { it.copy(errorMessage = "Новый пароль не должен совпадать со старым") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = changeTempPasswordUseCase(
                login = currentState.login,
                oldPass = currentState.oldPassword,
                newPass = currentState.newPassword
            )

            when (result) {
                is DomainResult.Success<*> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Пароль успешно изменен. Войдите с новым паролем."
                        )
                    }
                }
                is DomainResult.ValidationError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is DomainResult.UnauthorizedError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Неверный логин или временный пароль"
                        )
                    }
                }
                is DomainResult.ServerError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка сервера: код ${result.code}"
                        )
                    }
                }
                is DomainResult.NetworkError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка сети: ${result.message}"
                        )
                    }
                }
            }
        }
    }

    private fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(ChangeTempPasswordEvent.Exit)
        }
    }
}
