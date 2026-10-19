package ed.maevski.hwwach.ui.navflow.adminflow.hub

sealed class AdminHubAction {
    data object OnScreenShown : AdminHubAction()
    data object OnRegistrationRequestsClicked : AdminHubAction()
    data object OnBackPressed : AdminHubAction()
}

sealed class AdminHubEvent {
    data object NavigateToRegistrationRequests : AdminHubEvent()
    data object NavigateBack : AdminHubEvent()
}

data class AdminHubState(
    // null — счётчик ещё не загружен или не удалось загрузить
    val pendingRegistrationsCount: Int? = null
)
