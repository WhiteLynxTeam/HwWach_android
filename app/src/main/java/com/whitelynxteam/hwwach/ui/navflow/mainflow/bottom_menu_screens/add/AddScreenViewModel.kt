package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddScreenViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AddScreenState())
    val uiState: StateFlow<AddScreenState> = _uiState.asStateFlow()

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
                _uiState.update { it.copy(images = it.images + action.uri) }
            }
            is AddScreenAction.RemoveImage -> {
                _uiState.update { it.copy(images = it.images.filterIndexed { i, _ -> i != action.index }) }
            }
            is AddScreenAction.OnSubmitClicked -> {
                if (_uiState.value.name.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Название обязательно") }
                    return
                }
                if (_uiState.value.images.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = "Добавьте хотя бы одно фото") }
                    return
                }


            }
        }
    }
}