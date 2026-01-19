package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

data class AddScreenState(
    val currentMode: AddScreenTab = AddScreenTab.Gallery,
    val name: String = "",
    val category: Categories? = null,
    val inventoryNumber: String = "",
    val address: String = "",
    val comment: String = "",
    val images: List<String> = emptyList(),
    val errorMessage: String = ""
)