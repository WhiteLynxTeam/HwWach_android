package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

sealed class MainScreenAction {
    data class SelectTab(val tabIndex: Int) : MainScreenAction()
}

sealed class MainScreenEvent {
    data class NavigateToInnerScreen(val itemId: String) : MainScreenEvent()
}

data class MainScreenState(
    val selectedTabIndex: Int = 0
)