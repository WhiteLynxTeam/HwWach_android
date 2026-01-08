package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

sealed class AddScreenTab {
    data object Gallery : AddScreenTab()
    data object List : AddScreenTab()
}