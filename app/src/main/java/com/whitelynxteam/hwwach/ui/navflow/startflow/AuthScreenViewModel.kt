package com.whitelynxteam.hwwach.ui.navflow.startflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthScreenViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Input())
    val state = _state.asStateFlow()

    fun processCommand(command: AuthCommand) {
        when (command) {
            AuthCommand.Back -> {
                _state.update { AuthState.Finished }
            }

            is AuthCommand.InputLogin -> {
                _state.update { previousState ->
                    if (previousState is AuthState.Input) {
                        previousState.copy(
                            login = command.login,
                            errorAuth = null
                        )
                    } else {
                        AuthState.Input(login = command.login)
                    }
                }
            }

            is AuthCommand.InputPassword -> {
                _state.update { previousState ->
                    if (previousState is AuthState.Input) {
                        previousState.copy(
                            password = command.password,
                            errorAuth = null
                        )
                    } else {
                        AuthState.Input(password = command.password)
                    }
                }
            }

            AuthCommand.Login -> {
                viewModelScope.launch {
                    _state.update { previousState ->
                        if (previousState is AuthState.Input) {
                            val login = previousState.login
                            val password = previousState.password

                            // здесь должна быть реальная проверка (use case / репозиторий)
                            val isValid = login == "admin" && password == "1234"

                            if (isValid) {
                                AuthState.Finished
                            } else {
                                previousState.copy(
                                    errorAuth = "Неверный логин или пароль.\nПроверьте пожалуйста правильность написания"
                                )
                            }
                        } else {
                            previousState
                        }
                    }
                }
            }
        }
    }
}

sealed interface AuthCommand {

    data class InputLogin(val login: String) : AuthCommand
    data class InputPassword(val password: String) : AuthCommand
    data object Login : AuthCommand
    data object Back : AuthCommand
}

sealed interface AuthState {

    data class Input(
        val login: String = "",
        val password: String = "",
        val errorAuth: String? = null
    ) : AuthState

    data object Finished : AuthState
}