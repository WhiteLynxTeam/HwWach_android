package ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests

import ed.maevski.hwwach.domain.models.RegistrationRequest

sealed class RegistrationRequestsAction {
    data object OnScreenShown : RegistrationRequestsAction()
    data object OnRefresh : RegistrationRequestsAction()
    data class OnRequestClicked(val id: String) : RegistrationRequestsAction()
    data object OnBackPressed : RegistrationRequestsAction()
}

sealed class RegistrationRequestsEvent {
    data class NavigateToDetail(val id: String) : RegistrationRequestsEvent()
    data object NavigateBack : RegistrationRequestsEvent()
}

data class RegistrationRequestsState(
    val requests: List<RegistrationRequest> = emptyList(),
    // первичная загрузка — спиннер на весь экран
    val isLoading: Boolean = true,
    // pull-to-refresh — индикатор сверху, список остаётся
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
