package com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen

sealed class RegScreenAction {
    data class InputLastName(val name: String) : RegScreenAction()
    data class InputFirstName(val name: String) : RegScreenAction()
    data class InputMiddleName(val name: String) : RegScreenAction()
    data class InputPhone(val email: String) : RegScreenAction()
    data class InputPosition(val email: String) : RegScreenAction()
    data class InputLogin(val email: String) : RegScreenAction()
    data class InputPassword(val password: String) : RegScreenAction()
    data class InputConfirmPassword(val confirmPassword: String) : RegScreenAction()
    data object OnRegClicked : RegScreenAction()
    data object OnAuthClicked : RegScreenAction()
    data object OnBackClicked : RegScreenAction()
}

sealed class RegScreenEvent {
    data object NavigateToMain : RegScreenEvent()
    data object NavigateToAuth : RegScreenEvent()
    data object Exit : RegScreenEvent()
}

data class RegScreenState (
    val lastName: String = "",
    val firstName: String = "",
    val middleName : String = "",
    val phone: String = "",
    val position: String = "",
    val login: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val isLoading: Boolean = false,
)