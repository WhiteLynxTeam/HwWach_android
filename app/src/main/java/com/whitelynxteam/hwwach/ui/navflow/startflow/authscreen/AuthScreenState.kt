package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

data class AuthScreenState (
    val login: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)

//    data class Input(
//        val login: String = "",
//        val password: String = "",
//        val errorAuth: String? = null
//    ) : AuthScreenState
//
//    data object Finished : AuthScreenState
