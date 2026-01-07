package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

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
class MainScreenViewModel @Inject constructor(
) : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainScreenEvent>()
    val events: SharedFlow<MainScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: MainScreenAction) {
        when (action) {
            is MainScreenAction.OnBottomMenuItemClick -> onBottomMenuItemClick(action.tabIndex)
        }
    }

    private fun onBottomMenuItemClick(tabIndex: Int) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(selectedTabIndex = tabIndex)
            }
            _events.emit(MainScreenEvent.NavigateToBottomMenuItem(tabIndex))
        }
    }
}

