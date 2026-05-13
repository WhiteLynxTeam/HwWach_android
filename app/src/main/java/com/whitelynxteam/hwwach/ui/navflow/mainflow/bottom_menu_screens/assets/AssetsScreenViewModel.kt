package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

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
class AssetsScreenViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AssetsScreenState())
    val state: StateFlow<AssetsScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AssetsScreenEvent>()
    val events: SharedFlow<AssetsScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: AssetsScreenAction) {
        when (action) {
            is AssetsScreenAction.AddAssetClicked -> {
                // TODO: Implement add asset logic
                _state.update { it.copy(
                    isAdding = true,
                    errorMessage = ""
                ) }
                // Simulate async work
                viewModelScope.launch {
                    // TODO: Replace with actual use case
                    try {
                        // Simulate delay
                        Thread.sleep(1000)
                        _state.update { it.copy(
                            isAdding = false,
                            errorMessage = ""
                        )}
                        // TODO: Emit success event if needed
                    } catch (e: Exception) {
                        _state.update { it.copy(
                            isAdding = false,
                            addError = "Ошибка: ${e.message}"
                        )}
                    }
                }
            }
        }
    }
}