package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

sealed class AuthScreenAction {
    data class InputLogin(val login: String) : AuthScreenAction()
    data class InputPassword(val password: String) : AuthScreenAction()
    data object OnAuthClicked : AuthScreenAction()
    data object OnRegClicked : AuthScreenAction()
    data object OnForgotPasswordClicked : AuthScreenAction()
    data object OnBackClicked : AuthScreenAction()
}

sealed class AuthScreenEvent {
    data object NavigateToMain : AuthScreenEvent()
    data object NavigateToReg : AuthScreenEvent()
    data object NavigateToForgotPassword : AuthScreenEvent()
    data object Exit : AuthScreenEvent()
}

data class AuthScreenState (
    val login: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val registrationStatusMessage: String? = null,
)