package ed.maevski.hwwach.ui.navflow.adminflow.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.usecases.admin.GetPasswordResetsUseCase
import ed.maevski.hwwach.domain.usecases.admin.GetPendingRegistrationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminHubViewModel @Inject constructor(
    private val getPendingRegistrationsUseCase: GetPendingRegistrationsUseCase,
    private val getPasswordResetsUseCase: GetPasswordResetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminHubState())
    val state: StateFlow<AdminHubState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AdminHubEvent>()
    val events: SharedFlow<AdminHubEvent> = _events.asSharedFlow()

    fun handleAction(action: AdminHubAction) {
        when (action) {
            AdminHubAction.OnScreenShown -> loadCounters()
            AdminHubAction.OnRegistrationRequestsClicked -> viewModelScope.launch {
                _events.emit(AdminHubEvent.NavigateToRegistrationRequests)
            }
            AdminHubAction.OnPasswordResetsClicked -> viewModelScope.launch {
                _events.emit(AdminHubEvent.NavigateToPasswordResets)
            }
            AdminHubAction.OnBackPressed -> viewModelScope.launch {
                _events.emit(AdminHubEvent.NavigateBack)
            }
        }
    }

    // Счётчики обновляются при каждом возврате на хаб, чтобы бейдж был актуальным
    private fun loadCounters() {
        viewModelScope.launch {
            val result = getPendingRegistrationsUseCase()
            if (result is DomainResult.Success) {
                _state.update { it.copy(pendingRegistrationsCount = result.data.size) }
            }
        }
        viewModelScope.launch {
            val result = getPasswordResetsUseCase()
            if (result is DomainResult.Success) {
                _state.update { it.copy(pendingPasswordResetsCount = result.data.size) }
            }
        }
    }
}
