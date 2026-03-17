package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

sealed class MainScreenAction {
    data class OnBottomMenuItemClick(val tabIndex: Int) : MainScreenAction()
}

sealed class MainScreenEvent {
    data class NavigateToBottomMenuItem(val index: Int) : MainScreenEvent()
}

data class MainScreenState(
    // флаг готовности прорисовки главного экрана после вызова юзкейса проверки
    // таблиц устройств и фото -
    // если есть хоть одно устройство, то открываем экран устройств,
    // если нет устройств, то открываем экран фотографий и пусть делают инв фото
    val isReadyStartMainScreen: Boolean = false,
//    startMainScreenDestination: MainDestinationEnum = MainDestinationEnum.DEVICE_SCREEN,
    val selectedTabIndex: Int = 0,
    val bottomMenuItems: List<InnerMainFlowNavigation.Routes.BottomMenuDestination> = InnerMainFlowNavigation.Routes.menuRoutes
)