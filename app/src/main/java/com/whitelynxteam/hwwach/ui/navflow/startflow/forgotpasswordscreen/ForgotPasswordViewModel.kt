package com.whitelynxteam.hwwach.ui.navflow.startflow.forgotpasswordscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    fun handleAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.InputLogin -> {
                _state.update { it.copy(login = action.login, errorMessage = null, successMessage = null) }
            }
            ForgotPasswordAction.OnSubmitClicked -> onSubmitClicked()
            ForgotPasswordAction.OnBackClicked -> onBackClicked()
        }
    }

    private fun onSubmitClicked() {
        viewModelScope.launch {
            val currentState = _state.value
            
            if (currentState.login.isBlank()) {
                _state.update { it.copy(errorMessage = "Введите логин") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            // Имитация задержки отправки (так как API пока нет)
            delay(1000)

            // Заглушка успешного выполнения
            _state.update { 
                it.copy(
                    isLoading = false,
                    successMessage = "Инструкция по восстановлению пароля отправлена администратору (заглушка)."
                ) 
            }
        }
    }

    private fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(ForgotPasswordEvent.Exit)
        }
    }
}
