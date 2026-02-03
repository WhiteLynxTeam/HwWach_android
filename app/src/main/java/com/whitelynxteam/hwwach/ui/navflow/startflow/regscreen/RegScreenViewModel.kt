package com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegScreenViewModel @Inject constructor(
) : ViewModel() {
    private val _state = MutableStateFlow(RegScreenState())
    val state: StateFlow<RegScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegScreenEvent>()
    val events: SharedFlow<RegScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: RegScreenAction) {
        when (action) {
            RegScreenAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.Exit)
                }
            }

            is RegScreenAction.InputLogin -> {

            }

            is RegScreenAction.InputPassword -> {
            }

            RegScreenAction.OnAuthClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.NavigateToAuth)
                }
            }
            RegScreenAction.OnRegClicked -> {}

            else -> {}
        }
    }
}