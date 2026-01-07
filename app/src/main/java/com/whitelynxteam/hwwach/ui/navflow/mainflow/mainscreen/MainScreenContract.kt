package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.InnerMainFlowNavigation

sealed class MainScreenAction {
    data class OnBottomMenuItemClick(val tabIndex: Int) : MainScreenAction()
}

sealed class MainScreenEvent {
    data class NavigateToBottomMenuItem(val index: Int) : MainScreenEvent()
}

data class MainScreenState(
    val selectedTabIndex: Int = 0,
    val bottomMenuItems: List<InnerMainFlowNavigation.Routes> = InnerMainFlowNavigation.Routes.allRoutes
)