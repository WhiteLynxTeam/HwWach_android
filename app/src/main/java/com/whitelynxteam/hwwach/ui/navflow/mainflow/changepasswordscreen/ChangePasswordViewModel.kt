package com.whitelynxteam.hwwach.ui.navflow.mainflow.changepasswordscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.usecases.user.ChangePasswordUseCase
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
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChangePasswordEvent>()
    val events: SharedFlow<ChangePasswordEvent> = _events.asSharedFlow()

    fun handleAction(action: ChangePasswordAction) {
        when (action) {
            is ChangePasswordAction.InputOldPassword -> {
                _state.update { it.copy(oldPassword = action.oldPass, errorMessage = null, successMessage = null) }
            }
            is ChangePasswordAction.InputNewPassword -> {
                _state.update { it.copy(newPassword = action.newPass, errorMessage = null, successMessage = null) }
            }
            ChangePasswordAction.OnSubmitClicked -> onSubmitClicked()
            ChangePasswordAction.OnBackPressed -> onBackPressed()
        }
    }

    private fun onSubmitClicked() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.oldPassword.isBlank() || currentState.newPassword.isBlank()) {
                _state.update { it.copy(errorMessage = "Заполните все поля") }
                return@launch
            }

            if (currentState.oldPassword == currentState.newPassword) {
                _state.update { it.copy(errorMessage = "Новый пароль не должен совпадать со старым") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = changePasswordUseCase(
                oldPass = currentState.oldPassword,
                newPass = currentState.newPassword
            )

            when (result) {
                is DomainResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Пароль успешно изменен."
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
                            errorMessage = "Неверный текущий пароль или сессия устарела"
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

    private fun onBackPressed() {
        viewModelScope.launch {
            _events.emit(ChangePasswordEvent.Exit)
        }
    }
}
