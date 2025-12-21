package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AuthScreenViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AuthScreenState())
    val state: StateFlow<AuthScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthScreenEvent>()
    val events: SharedFlow<AuthScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: AuthScreenAction) {
        when (action) {
            AuthScreenAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.emit(AuthScreenEvent.Exit)
                }
            }

            is AuthScreenAction.InputLogin -> {
                _state.update { it.copy(login = action.login) }
            }

            is AuthScreenAction.InputPassword -> {
                _state.update { it.copy(login = action.password) }
            }

            AuthScreenAction.OnAuthClicked -> onAuthClicked()
        }
    }

    private fun onAuthClicked() {
        viewModelScope.launch {
            //= защита от race condition, пользователь может ввести логин между проверкой и запросом
            //= поэтому работаем с currentState
            val currentState = _state.value

            //= проверка на то, если пользователь нажал авторизацию до ввода логина и пароля
            if (currentState.login.isBlank() || currentState.password.isBlank()) {
                _state.update { it.copy(errorMessage = "Заполните все поля") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null) }

//            val result = loginWithProfileUseCase(username = currentState.login, password = currentState.password)

            _state.update { it.copy(isLoading = false) }

/*            _state.update { previousState ->
                if (previousState is AuthScreenState.Input) {
                    val login = previousState.login
                    val password = previousState.password

                    // здесь должна быть реальная проверка (use case / репозиторий)
                    val isValid = login == "admin" && password == "1234"

                    if (isValid) {
                        AuthScreenState.Finished
                    } else {
                        previousState.copy(
                            errorAuth = "Неверный логин или пароль.\nПроверьте пожалуйста правильность написания"
                        )
                    }
                } else {
                    previousState
                }
            }*/
        }

    }
}

