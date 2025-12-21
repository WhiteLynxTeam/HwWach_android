package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

sealed class AuthScreenEvent {
    data object NavigateToMain : AuthScreenEvent()
    data object Exit : AuthScreenEvent()
}