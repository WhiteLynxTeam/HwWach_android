package ed.maevski.hwwach.ui.navflow.mainflow.assignments

import ed.maevski.hwwach.domain.models.Assignment

// «Мне назначено»: всё применяется сразу, здесь можно подтвердить или отказаться с причиной.
// Актив при отказе остаётся на месте — отказ уходит админу «на разбор»
sealed class AssignmentsAction {
    data object Refresh : AssignmentsAction()
    data class Accept(val roleId: String) : AssignmentsAction()
    data class StartDispute(val roleId: String) : AssignmentsAction()
    data class InputReason(val reason: String) : AssignmentsAction()
    data object ConfirmDispute : AssignmentsAction()
    data object CancelDispute : AssignmentsAction()
}

data class AssignmentsState(
    val items: List<Assignment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String = "",
    // открыт диалог отказа
    val disputeRoleId: String? = null,
    val disputeReason: String = "",
    val busyRoleId: String? = null,
)
