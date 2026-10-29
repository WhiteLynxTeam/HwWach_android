package ed.maevski.hwwach.ui.navflow.adminflow.passwordresets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.PasswordResetRequest
import ed.maevski.hwwach.domain.usecases.admin.ApprovePasswordResetUseCase
import ed.maevski.hwwach.domain.usecases.admin.GetPasswordResetsUseCase
import ed.maevski.hwwach.domain.usecases.admin.RejectPasswordResetUseCase
import ed.maevski.hwwach.ui.navflow.adminflow.toAdminErrorMessage
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
class PasswordResetsViewModel @Inject constructor(
    private val getPasswordResetsUseCase: GetPasswordResetsUseCase,
    private val approvePasswordResetUseCase: ApprovePasswordResetUseCase,
    private val rejectPasswordResetUseCase: RejectPasswordResetUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordResetsState())
    val state: StateFlow<PasswordResetsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PasswordResetsEvent>()
    val events: SharedFlow<PasswordResetsEvent> = _events.asSharedFlow()

    fun handleAction(action: PasswordResetsAction) {
        when (action) {
            PasswordResetsAction.OnScreenShown -> loadRequests(isRefresh = false)
            PasswordResetsAction.OnRefresh -> loadRequests(isRefresh = true)
            PasswordResetsAction.OnBackPressed -> viewModelScope.launch {
                _events.emit(PasswordResetsEvent.NavigateBack)
            }
            is PasswordResetsAction.OnResetClicked -> _state.update {
                it.copy(resetTarget = action.request, passwordInput = "", dialogError = null)
            }
            is PasswordResetsAction.InputPassword -> _state.update {
                it.copy(passwordInput = action.value.replace(" ", ""), dialogError = null)
            }
            PasswordResetsAction.OnConfirmReset -> confirmReset()
            is PasswordResetsAction.OnRejectClicked -> reject(action.request)
            PasswordResetsAction.OnDismissDialog -> _state.update {
                if (it.processingId != null) it else it.copy(resetTarget = null, done = null, dialogError = null)
            }
        }
    }

    private fun loadRequests(isRefresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh && it.requests.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }
            val result = getPasswordResetsUseCase()
            _state.update {
                when (result) {
                    is DomainResult.Success -> it.copy(requests = result.data, isLoading = false, isRefreshing = false)
                    else -> it.copy(isLoading = false, isRefreshing = false, errorMessage = result.toAdminErrorMessage())
                }
            }
        }
    }

    private fun confirmReset() {
        val target = _state.value.resetTarget ?: return
        if (_state.value.processingId != null) return
        viewModelScope.launch {
            _state.update { it.copy(processingId = target.id, dialogError = null) }
            when (val result = approvePasswordResetUseCase(target.id, _state.value.passwordInput)) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        processingId = null,
                        resetTarget = null,
                        requests = it.requests.filterNot { r -> r.id == target.id },
                        done = PasswordResetDone(target.login, result.data.tempPassword, result.data.isDefault)
                    )
                }
                else -> _state.update { it.copy(processingId = null, dialogError = result.toAdminErrorMessage()) }
            }
        }
    }

    // Без подтверждения: пользователь при необходимости отправит заявку снова
    private fun reject(request: PasswordResetRequest) {
        if (_state.value.processingId != null) return
        viewModelScope.launch {
            _state.update { it.copy(processingId = request.id) }
            val result = rejectPasswordResetUseCase(request.id)
            _state.update {
                if (result is DomainResult.Success) {
                    it.copy(processingId = null, requests = it.requests.filterNot { r -> r.id == request.id })
                } else {
                    it.copy(processingId = null)
                }
            }
            _events.emit(
                PasswordResetsEvent.ShowMessage(
                    if (result is DomainResult.Success) "Заявка отклонена" else result.toAdminErrorMessage() ?: "Ошибка"
                )
            )
        }
    }
}
