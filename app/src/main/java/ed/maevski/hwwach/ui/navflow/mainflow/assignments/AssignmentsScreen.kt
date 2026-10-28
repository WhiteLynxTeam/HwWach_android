package ed.maevski.hwwach.ui.navflow.mainflow.assignments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ed.maevski.hwwach.domain.models.AckStatus
import ed.maevski.hwwach.domain.models.AssetLabels
import ed.maevski.hwwach.domain.models.Assignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    state: AssignmentsState,
    onAction: (AssignmentsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Мне назначено") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(AssignmentsAction.Refresh) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.items.isEmpty() -> Text(
                    text = if (state.errorMessage.isNotBlank()) state.errorMessage else "Вам ничего не назначено",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Назначения действуют сразу. Подтвердите или откажитесь — при отказе актив остаётся, где его поставили, а администратор разберётся.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.errorMessage.isNotBlank()) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    items(state.items, key = { it.role.id }) { item ->
                        AssignmentCard(item = item, busy = state.busyRoleId == item.role.id, onAction = onAction)
                    }
                }
            }
        }
    }

    if (state.disputeRoleId != null) {
        AlertDialog(
            onDismissRequest = { onAction(AssignmentsAction.CancelDispute) },
            title = { Text("Отказаться") },
            text = {
                Column {
                    Text("Почему? Администратор увидит причину.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.disputeReason,
                        onValueChange = { onAction(AssignmentsAction.InputReason(it)) },
                        placeholder = { Text("например: этого актива у меня нет") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(AssignmentsAction.ConfirmDispute) }) { Text("Отказаться") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AssignmentsAction.CancelDispute) }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun AssignmentCard(item: Assignment, busy: Boolean, onAction: (AssignmentsAction) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.asset?.name ?: "Актив", style = MaterialTheme.typography.titleMedium)
            item.asset?.inventoryNum?.takeIf { it.isNotBlank() }?.let {
                Text(text = "Инв. № $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (item.role.isMol) "Числится за вами" else "Вы работаете с активом",
                style = MaterialTheme.typography.bodyMedium
            )
            item.asset?.let {
                Text(
                    text = "${AssetLabels.accounting(it.accountingStatus)} · ${AssetLabels.condition(it.condition)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Статус: ${AssetLabels.ack(item.role.ackStatus)}" + (item.role.ackReason?.let { " — «$it»" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = if (item.role.ackStatus == AckStatus.DISPUTED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.role.ackStatus != AckStatus.ACCEPTED) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = !busy, onClick = { onAction(AssignmentsAction.Accept(item.role.id)) }) {
                        Text("Подтвердить")
                    }
                    if (item.role.ackStatus != AckStatus.DISPUTED) {
                        OutlinedButton(enabled = !busy, onClick = { onAction(AssignmentsAction.StartDispute(item.role.id)) }) {
                            Text("Отказаться")
                        }
                    }
                }
            }
        }
    }
}
