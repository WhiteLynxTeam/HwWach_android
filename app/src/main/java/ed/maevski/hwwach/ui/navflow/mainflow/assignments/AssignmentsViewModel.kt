package ed.maevski.hwwach.ui.navflow.mainflow.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRolesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val rolesRepository: IAssetRolesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentsState())
    val state: StateFlow<AssignmentsState> = _state.asStateFlow()

    init {
        load()
    }

    fun handleAction(action: AssignmentsAction) {
        when (action) {
            AssignmentsAction.Refresh -> load()
            is AssignmentsAction.Accept -> ack(action.roleId, accept = true, reason = null)
            is AssignmentsAction.StartDispute -> _state.update { it.copy(disputeRoleId = action.roleId, disputeReason = "") }
            is AssignmentsAction.InputReason -> _state.update { it.copy(disputeReason = action.reason) }
            AssignmentsAction.CancelDispute -> _state.update { it.copy(disputeRoleId = null, disputeReason = "") }
            AssignmentsAction.ConfirmDispute -> {
                val s = _state.value
                val roleId = s.disputeRoleId ?: return
                if (s.disputeReason.isBlank()) {
                    _state.update { it.copy(errorMessage = "Укажите причину отказа") }
                    return
                }
                _state.update { it.copy(disputeRoleId = null) }
                ack(roleId, accept = false, reason = s.disputeReason)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = "") }
            when (val result = rolesRepository.myAssignments()) {
                is DomainResult.Success -> _state.update { it.copy(isLoading = false, items = result.data) }
                is DomainResult.NetworkError -> _state.update { it.copy(isLoading = false, errorMessage = "Нет связи с сервером") }
                else -> _state.update { it.copy(isLoading = false, errorMessage = "Не удалось загрузить назначения") }
            }
        }
    }

    private fun ack(roleId: String, accept: Boolean, reason: String?) {
        viewModelScope.launch {
            _state.update { it.copy(busyRoleId = roleId, errorMessage = "") }
            when (val result = rolesRepository.ack(roleId, accept, reason)) {
                is DomainResult.Success -> {
                    _state.update { it.copy(busyRoleId = null) }
                    load()
                }
                is DomainResult.ValidationError -> _state.update { it.copy(busyRoleId = null, errorMessage = result.message) }
                else -> _state.update { it.copy(busyRoleId = null, errorMessage = "Не удалось сохранить, попробуйте позже") }
            }
        }
    }
}
