package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

sealed class AuthScreenAction {
    data class InputLogin(val login: String) : AuthScreenAction()
    data class InputPassword(val password: String) : AuthScreenAction()
    data object OnAuthClicked : AuthScreenAction()
    data object OnBackClicked : AuthScreenAction()
}