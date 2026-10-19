package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import ed.maevski.hwwach.domain.models.User

sealed class ProfileAction {
    data object OnChangePasswordClicked : ProfileAction()
    data object OnLogoutClicked : ProfileAction()
    data object OnAdminClicked : ProfileAction()
}

sealed class ProfileEvent {
    data object NavigateToChangePassword : ProfileEvent()
    data object NavigateToLogin : ProfileEvent()
    data object NavigateToAdmin : ProfileEvent()
}

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true
) {
    // Только для показа пункта меню; доступ к админским запросам проверяет сервер
    val isAdmin: Boolean
        get() = user?.role == "ADMIN"
}
