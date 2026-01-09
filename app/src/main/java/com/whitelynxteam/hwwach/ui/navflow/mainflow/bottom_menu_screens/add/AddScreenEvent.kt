package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

sealed class AddScreenEvent {
    data object ShowSuccessMessage : AddScreenEvent()
    data class ShowErrorMessage(val message: String) : AddScreenEvent()
    data object NavigateBack : AddScreenEvent()
    data object OpenImagePicker : AddScreenEvent()
}