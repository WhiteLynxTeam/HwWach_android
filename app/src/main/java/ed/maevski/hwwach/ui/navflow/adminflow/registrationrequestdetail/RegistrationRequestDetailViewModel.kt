package ed.maevski.hwwach.ui.navflow.adminflow.registrationrequestdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.usecases.admin.ApproveRegistrationUseCase
import ed.maevski.hwwach.domain.usecases.admin.GetRegistrationUseCase
import ed.maevski.hwwach.domain.usecases.admin.RejectRegistrationUseCase
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
class RegistrationRequestDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRegistrationUseCase: GetRegistrationUseCase,
    private val approveRegistrationUseCase: ApproveRegistrationUseCase,
    private val rejectRegistrationUseCase: RejectRegistrationUseCase
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(RegistrationRequestDetailState())
    val state: StateFlow<RegistrationRequestDetailState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegistrationRequestDetailEvent>()
    val events: SharedFlow<RegistrationRequestDetailEvent> = _events.asSharedFlow()

    init {
        loadRequest()
    }

    fun handleAction(action: RegistrationRequestDetailAction) {
        when (action) {
            RegistrationRequestDetailAction.OnRetry -> loadRequest()
            RegistrationRequestDetailAction.OnApproveClicked ->
                _state.update { it.copy(dialog = RegistrationRequestDialog.APPROVE) }
            RegistrationRequestDetailAction.OnRejectClicked ->
                _state.update { it.copy(dialog = RegistrationRequestDialog.REJECT) }
            is RegistrationRequestDetailAction.OnRejectReasonChanged ->
                _state.update { it.copy(rejectReason = action.reason) }
            RegistrationRequestDetailAction.OnDialogDismissed ->
                _state.update { if (it.isProcessing) it else it.copy(dialog = null) }
            RegistrationRequestDetailAction.OnApproveConfirmed -> processRequest(
                successMessage = "Регистрация одобрена"
            ) { approveRegistrationUseCase(requestId) }
            RegistrationRequestDetailAction.OnRejectConfirmed -> processRequest(
                successMessage = "Заявка отклонена"
            ) { rejectRegistrationUseCase(requestId, _state.value.rejectReason) }
            RegistrationRequestDetailAction.OnBackPressed -> viewModelScope.launch {
                _events.emit(RegistrationRequestDetailEvent.NavigateBack)
            }
        }
    }

    private fun loadRequest() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadErrorMessage = null) }

            when (val result = getRegistrationUseCase(requestId)) {
                is DomainResult.Success -> _state.update {
                    it.copy(request = result.data, isLoading = false)
                }
                else -> _state.update {
                    it.copy(isLoading = false, loadErrorMessage = result.toAdminErrorMessage())
                }
            }
        }
    }

    private fun processRequest(
        successMessage: String,
        call: suspend () -> DomainResult<Unit>
    ) {
        if (_state.value.isProcessing) return

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            val result = call()

            _state.update { it.copy(isProcessing = false, dialog = null) }

            when (result) {
                is DomainResult.Success -> {
                    _events.emit(RegistrationRequestDetailEvent.ShowMessage(successMessage))
                    _events.emit(RegistrationRequestDetailEvent.NavigateBack)
                }
                // 4xx: чаще всего заявку уже обработали (например, в Wails) — возвращаемся к списку
                is DomainResult.ValidationError -> {
                    _events.emit(RegistrationRequestDetailEvent.ShowMessage("Заявка уже обработана или недоступна"))
                    _events.emit(RegistrationRequestDetailEvent.NavigateBack)
                }
                else -> {
                    result.toAdminErrorMessage()?.let {
                        _events.emit(RegistrationRequestDetailEvent.ShowMessage(it))
                    }
                }
            }
        }
    }
}
