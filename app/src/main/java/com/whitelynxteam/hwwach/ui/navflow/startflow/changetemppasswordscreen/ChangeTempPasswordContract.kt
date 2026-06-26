package com.whitelynxteam.hwwach.ui.navflow.startflow.changetemppasswordscreen

sealed class ChangeTempPasswordAction {
    data class InputLogin(val login: String) : ChangeTempPasswordAction()
    data class InputOldPassword(val oldPass: String) : ChangeTempPasswordAction()
    data class InputNewPassword(val newPass: String) : ChangeTempPasswordAction()
    data object OnSubmitClicked : ChangeTempPasswordAction()
    data object OnBackClicked : ChangeTempPasswordAction()
}

sealed class ChangeTempPasswordEvent {
    data object Exit : ChangeTempPasswordEvent()
}

data class ChangeTempPasswordState(
    val login: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
