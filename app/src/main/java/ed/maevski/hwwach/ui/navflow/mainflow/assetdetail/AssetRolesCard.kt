package ed.maevski.hwwach.ui.navflow.mainflow.assetdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ed.maevski.hwwach.domain.models.AssetLabels

// За кем числится актив и кто с ним работает (3.2). Передачи применяются сразу,
// получатель потом подтверждает или отказывается в «Мне назначено»
@Composable
fun AssetRolesCard(state: AssetDetailScreenState, onAction: (AssetDetailScreenAction) -> Unit) {
    val info = state.serverInfo
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            if (info == null) {
                Text(
                    text = if (state.asset?.serverUuid == null) "Ответственные появятся после синхронизации карточки"
                    else "Ответственные загружаются…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }
            Text(text = "Числится за (МОЛ)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = info.mol?.let { "${state.personName(it.personId)} · ${AssetLabels.ack(it.ackStatus)}" } ?: "Без МОЛ",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Работают с активом", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (info.users.isEmpty()) {
                Text(text = "—", style = MaterialTheme.typography.bodyLarge)
            } else {
                info.users.forEach { u ->
                    Text(text = "${state.personName(u.personId)} · ${AssetLabels.ack(u.ackStatus)}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (state.canManageRoles && info.accountingStatus != "decommissioned") {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { onAction(AssetDetailScreenAction.OpenDialog(AssetDialog.TRANSFER_MOL)) }) {
                    Text("Передать МОЛ")
                }
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { onAction(AssetDetailScreenAction.OpenDialog(AssetDialog.ADD_USER)) }) {
                    Text("Назначить работающего")
                }
            }
            if (info.accountingStatus != "decommissioned") {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { onAction(AssetDetailScreenAction.OpenDialog(AssetDialog.REPORT_FAULT)) }) {
                    Text("Сообщить о неисправности")
                }
            }
            if (state.infoMessage.isNotBlank()) {
                Text(
                    text = state.infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    state.dialog?.let { dialog -> AssetDialogView(state, dialog, onAction) }
}

@Composable
private fun AssetDialogView(state: AssetDetailScreenState, dialog: AssetDialog, onAction: (AssetDetailScreenAction) -> Unit) {
    val info = state.serverInfo
    // передать МОЛ — только человеку с признаком МОЛ; работать с активом может любой
    val candidates = state.persons.values
        .filter { it.isActive }
        .filter { if (dialog == AssetDialog.TRANSFER_MOL) it.isMol && it.id != info?.mol?.personId else true }
        .filter { p -> dialog != AssetDialog.ADD_USER || info?.users?.none { it.personId == p.id } != false }
        .sortedBy { it.fullName }

    AlertDialog(
        onDismissRequest = { onAction(AssetDetailScreenAction.CloseDialog) },
        title = {
            Text(
                when (dialog) {
                    AssetDialog.TRANSFER_MOL -> "Передать МОЛ"
                    AssetDialog.ADD_USER -> "Кто работает с активом"
                    AssetDialog.REPORT_FAULT -> "Сообщить о неисправности"
                }
            )
        },
        text = {
            Column {
                if (dialog == AssetDialog.REPORT_FAULT) {
                    Text("Актив отметится «неисправен», МОЛ и администратор увидят заявку.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.dialogText,
                        onValueChange = { onAction(AssetDetailScreenAction.InputText(it)) },
                        placeholder = { Text("Что случилось") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    if (candidates.isEmpty()) {
                        Text(
                            if (dialog == AssetDialog.TRANSFER_MOL) "В организации нет других МОЛ — признак ставит администратор"
                            else "Нет подходящих людей",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(candidates, key = { it.id }) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAction(AssetDetailScreenAction.SelectPerson(p.id)) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = state.selectedPersonId == p.id, onClick = { onAction(AssetDetailScreenAction.SelectPerson(p.id)) })
                                Column {
                                    Text(p.fullName, style = MaterialTheme.typography.bodyMedium)
                                    p.position?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    if (dialog == AssetDialog.TRANSFER_MOL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.dialogText,
                            onValueChange = { onAction(AssetDetailScreenAction.InputText(it)) },
                            placeholder = { Text("Комментарий (необязательно)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (state.infoMessage.isNotBlank()) {
                    Text(state.infoMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(enabled = !state.isSaving, onClick = { onAction(AssetDetailScreenAction.ConfirmDialog) }) {
                Text(if (dialog == AssetDialog.REPORT_FAULT) "Отправить" else "Назначить")
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AssetDetailScreenAction.CloseDialog) }) { Text("Отмена") }
        }
    )
}
