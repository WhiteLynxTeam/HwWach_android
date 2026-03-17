package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.models.MainDestinationEnum
import com.whitelynxteam.hwwach.domain.usecases.GetStartMainScreenDestinationUseCase
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
    private val getStartMainScreenDestinationUseCase: GetStartMainScreenDestinationUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainScreenEvent>()
    val events: SharedFlow<MainScreenEvent> = _events.asSharedFlow()

    init {
        determineStartScreen()
    }

    // Определяет стартовый экран
    private fun determineStartScreen() {
        viewModelScope.launch {
            val startPageIndex = when (getStartMainScreenDestinationUseCase()) {
                // если нет устройств в бд или пустая база, то открываем фото
                MainDestinationEnum.PHOTO_SCREEN -> 1
                // в другом случае всегда открываем список устройств, всегда
                else -> 0
            }

            // 1. Обновляем стейт (чтобы BottomBar выделил нужную иконку)
            _state.update { it.copy(selectedTabIndex = startPageIndex) }

            // 2. Посылаем разовое событие для навигации внутреннего контроллера
            _events.emit(MainScreenEvent.NavigateToBottomMenuItem(startPageIndex))
        }
    }

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

