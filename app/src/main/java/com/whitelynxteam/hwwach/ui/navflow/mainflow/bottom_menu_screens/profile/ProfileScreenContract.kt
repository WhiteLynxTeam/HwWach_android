package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import com.whitelynxteam.hwwach.domain.models.User

sealed class ProfileAction {
    data object OnChangePasswordClicked : ProfileAction()
    data object OnLogoutClicked : ProfileAction()
}

sealed class ProfileEvent {
    data object NavigateToChangePassword : ProfileEvent()
    data object NavigateToLogin : ProfileEvent()
}

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true
)
