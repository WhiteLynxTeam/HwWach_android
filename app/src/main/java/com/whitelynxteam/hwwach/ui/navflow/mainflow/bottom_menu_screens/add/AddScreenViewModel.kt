package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

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
class AddScreenViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AddScreenState())
    val uiState: StateFlow<AddScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddScreenEvent>()
    val events: SharedFlow<AddScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: AddScreenAction) {
        when (action) {
            is AddScreenAction.SwitchMode -> {
                _uiState.update { it.copy(currentMode = action.mode) }
            }
            is AddScreenAction.InputName -> {
                _uiState.update { it.copy(name = action.value) }
            }
            is AddScreenAction.InputCategory -> {
                _uiState.update { it.copy(category = action.value) }
            }
            is AddScreenAction.InputInventoryNumber -> {
                _uiState.update { it.copy(inventoryNumber = action.value) }
            }
            is AddScreenAction.InputAddress -> {
                _uiState.update { it.copy(address = action.value) }
            }
            is AddScreenAction.InputComment -> {
                _uiState.update { it.copy(comment = action.value) }
            }
            is AddScreenAction.AddImage -> {
                _uiState.update { state ->
                    // Добавляем фото только если текущее количество меньше 10
                    if (state.images.size < 10) {
                        state.copy(images = state.images + action.uri)
                    } else {
                        // Опционально: можно вывести ошибку "Максимум 10 фото"
                        state.copy(errorMessage = "Нельзя добавить больше 10 изображений")
                    }
                }
               // _uiState.update { it.copy(images = it.images + action.uri) }
            }
            is AddScreenAction.RemoveImage -> {
                _uiState.update { it.copy(images = it.images.filterIndexed { i, _ -> i != action.index }) }
            }
            is AddScreenAction.OnSubmitClicked -> {
                validateAndSubmit()
            }

            AddScreenAction.OpenImagePicker -> {
                viewModelScope.launch {
                    _events.emit(AddScreenEvent.OpenImagePicker)
                }
            }

            AddScreenAction.OpenCamera -> {
                viewModelScope.launch {
                    _events.emit(AddScreenEvent.OpenCamera)
                }
            }
        }
    }

    private fun validateAndSubmit() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.name.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Название обязательно") }
                return@launch
            }
            if (currentState.images.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Добавьте хотя бы одно фото") }
                return@launch
            }

            // Здесь должен быть вызов use case для сохранения данных
            // После успешного сохранения отправляем событие
            _events.emit(AddScreenEvent.ShowSuccessMessage)
            _events.emit(AddScreenEvent.NavigateBack)
        }
    }
}