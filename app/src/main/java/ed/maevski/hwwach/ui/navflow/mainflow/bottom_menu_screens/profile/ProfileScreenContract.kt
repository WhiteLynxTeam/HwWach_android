package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import ed.maevski.hwwach.domain.models.User

sealed class ProfileAction {
    data object OnChangePasswordClicked : ProfileAction()
    data object OnLogoutClicked : ProfileAction()
    data object OnAdminClicked : ProfileAction()
    data object OnAssignmentsClicked : ProfileAction()
    data object OnRequestMolClicked : ProfileAction()
}

sealed class ProfileEvent {
    data object NavigateToChangePassword : ProfileEvent()
    data object NavigateToLogin : ProfileEvent()
    data object NavigateToAdmin : ProfileEvent()
    data object NavigateToAssignments : ProfileEvent()
}

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val molRequestMessage: String = "",
) {
    // Только для показа пункта меню; доступ к админским запросам проверяет сервер.
    // Меню — по правам, а не по роли
    val isAdmin: Boolean
        get() = user?.can("accounts.manage") == true
}
