package com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.User
import com.whitelynxteam.hwwach.domain.usecases.RegApiUseCase
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
class RegScreenViewModel @Inject constructor(
    private val regApiUseCase: RegApiUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RegScreenState())
    val state: StateFlow<RegScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegScreenEvent>()
    val events: SharedFlow<RegScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: RegScreenAction) {
        when (action) {
            is RegScreenAction.InputLastName -> {
                _state.update { it.copy(lastName = action.name) }
            }

            is RegScreenAction.InputFirstName -> {
                _state.update { it.copy(firstName = action.name) }
            }

            is RegScreenAction.InputMiddleName -> {
                _state.update { it.copy(middleName = action.name) }
            }

            is RegScreenAction.InputPhone -> {
                _state.update { it.copy(phone = action.email) }
            }

            is RegScreenAction.InputPosition -> {
                _state.update { it.copy(position = action.email) }
            }

            is RegScreenAction.InputLogin -> {
                _state.update { it.copy(login = action.email) }
            }

            is RegScreenAction.InputPassword -> {
                _state.update { it.copy(password = action.password) }
            }

            is RegScreenAction.InputConfirmPassword -> {
                // TODO: Implement confirm password logic
            }

            RegScreenAction.OnAuthClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.NavigateToAuth)
                }
            }
            RegScreenAction.OnRegClicked -> onRegClicked()

            RegScreenAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.Exit)
                }
            }
        }
    }

    private fun onRegClicked() {
        viewModelScope.launch {
            val currentState = _state.value

            // Check if required fields are filled
            if (currentState.lastName.isBlank() ||
                currentState.firstName.isBlank() ||
                currentState.login.isBlank() ||
                currentState.password.isBlank()) {
                _state.update { it.copy(errorMessage = "Заполните все обязательные поля") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = "") }

            // Create user object from state
            val user = User(
                username = currentState.login,
                password = currentState.password,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                middleName = currentState.middleName,
                phone = currentState.phone,
                position = currentState.position
            )

            when (regApiUseCase(user)) {
                is DomainResult.Success -> {
                    _events.emit(RegScreenEvent.NavigateToAuth)
                }
                else -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка при регистрации"
                        )
                    }
                }
            }
        }
    }
}