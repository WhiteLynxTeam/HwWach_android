package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

sealed class AddScreenAction {
    data class SwitchMode(val mode: AddScreenTab) : AddScreenAction() // ← правильно

    data class InputName(val value: String) : AddScreenAction()
    data class InputCategory(val value: String) : AddScreenAction()
    data class InputInventoryNumber(val value: String) : AddScreenAction()
    data class InputAddress(val value: String) : AddScreenAction()
    data class InputComment(val value: String) : AddScreenAction()

    data class AddImage(val uri: String) : AddScreenAction()
    data class RemoveImage(val index: Int) : AddScreenAction()

    data object OnSubmitClicked : AddScreenAction()
}