package ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.usecases.admin.GetPendingRegistrationsUseCase
import ed.maevski.hwwach.ui.navflow.adminflow.toAdminErrorMessage
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
class RegistrationRequestsViewModel @Inject constructor(
    private val getPendingRegistrationsUseCase: GetPendingRegistrationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationRequestsState())
    val state: StateFlow<RegistrationRequestsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegistrationRequestsEvent>()
    val events: SharedFlow<RegistrationRequestsEvent> = _events.asSharedFlow()

    fun handleAction(action: RegistrationRequestsAction) {
        when (action) {
            // Перезагружаем при каждом показе: после одобрения/отклонения на детальном экране
            // заявка должна исчезнуть из списка
            RegistrationRequestsAction.OnScreenShown -> loadRequests(isRefresh = false)
            RegistrationRequestsAction.OnRefresh -> loadRequests(isRefresh = true)
            is RegistrationRequestsAction.OnRequestClicked -> viewModelScope.launch {
                _events.emit(RegistrationRequestsEvent.NavigateToDetail(action.id))
            }
            RegistrationRequestsAction.OnBackPressed -> viewModelScope.launch {
                _events.emit(RegistrationRequestsEvent.NavigateBack)
            }
        }
    }

    private fun loadRequests(isRefresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    // спиннер на весь экран только если показывать пока нечего
                    isLoading = !isRefresh && it.requests.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val result = getPendingRegistrationsUseCase()

            _state.update {
                when (result) {
                    is DomainResult.Success -> it.copy(
                        requests = result.data,
                        isLoading = false,
                        isRefreshing = false
                    )
                    else -> it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = result.toAdminErrorMessage()
                    )
                }
            }
        }
    }
}
