package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.usecases.CheckRegistrationUseCase
import com.whitelynxteam.hwwach.domain.usecases.LoginWithProfileUseCase
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
class AuthScreenViewModel @Inject constructor(
    private val loginWithProfileUseCase: LoginWithProfileUseCase,
    private val checkRegistrationUseCase: CheckRegistrationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthScreenState())
    val state: StateFlow<AuthScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthScreenEvent>()
    val events: SharedFlow<AuthScreenEvent> = _events.asSharedFlow()

    init {
        // Проверяем статус регистрации при инициализации ViewModel
        viewModelScope.launch {
            val regStatus = checkRegistrationUseCase()
            if (regStatus is DomainResult.Success && regStatus.data.uuid != null) {
                val statusMessage = "Логин: ${regStatus.data.login} ${regStatus.data.status?.toDisplayString()}"
                _state.update { it.copy(registrationStatusMessage = statusMessage) }
            }
        }
    }

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
                _state.update { it.copy(password = action.password) }
            }

            AuthScreenAction.OnAuthClicked -> onAuthClicked()
            AuthScreenAction.OnRegClicked -> onRegClicked()
        }
    }

    private fun onRegClicked() {
        viewModelScope.launch {
            _events.emit(AuthScreenEvent.NavigateToReg)
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

            _state.update { it.copy(isLoading = true, errorMessage = "") }

            val result = loginWithProfileUseCase(
                login = currentState.login,
                password = currentState.password
            )

            println("AuthScreenViewModel result=$result")

            when (result) {
                is DomainResult.Success<*> -> {
                    println("AuthScreenViewModel DomainResult.Success<*>")

                    _events.emit(AuthScreenEvent.NavigateToMain)
                }
                is DomainResult.UnauthorizedError -> {
                    println("AuthScreenViewModel DomainResult.UnauthorizedError")

                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Неверный логин или пароль.\nПроверьте пожалуйста правильность написания"
                        ) 
                    }
                }
                else -> {
                    println("AuthScreenViewModel DomainResult - else")

                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка сервера авторизации"
                        ) 
                    }
                }
            }
        }
    }
}

