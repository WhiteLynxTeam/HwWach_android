package com.whitelynxteam.hwwach.ui.navflow.mainflow.changepasswordscreen

sealed class ChangePasswordAction {
    data class InputOldPassword(val oldPass: String) : ChangePasswordAction()
    data class InputNewPassword(val newPass: String) : ChangePasswordAction()
    data object OnSubmitClicked : ChangePasswordAction()
    data object OnBackPressed : ChangePasswordAction()
}

sealed class ChangePasswordEvent {
    data object Exit : ChangePasswordEvent()
}

data class ChangePasswordState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
