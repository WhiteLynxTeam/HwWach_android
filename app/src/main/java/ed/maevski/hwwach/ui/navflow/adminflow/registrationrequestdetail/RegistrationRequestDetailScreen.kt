package ed.maevski.hwwach.ui.navflow.adminflow.registrationrequestdetail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.domain.models.RegStatusEnum
import ed.maevski.hwwach.domain.models.RegistrationRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationRequestDetailScreen(
    modifier: Modifier = Modifier,
    state: RegistrationRequestDetailState,
    onAction: (RegistrationRequestDetailAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // сверху отступ даёт TopAppBar, снизу — кнопки навигации и клавиатура (edge-to-edge)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
    ) {
        TopAppBar(
            title = { Text(text = "Заявка на регистрацию") },
            navigationIcon = {
                IconButton(onClick = { onAction(RegistrationRequestDetailAction.OnBackPressed) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        )

        val request = state.request
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            request == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.loadErrorMessage ?: "Заявка не найдена",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onAction(RegistrationRequestDetailAction.OnRetry) }) {
                        Text(text = "Повторить")
                    }
                }
            }
            else -> {
                RegistrationRequestDetailContent(
                    request = request,
                    isProcessing = state.isProcessing,
                    onAction = onAction
                )
            }
        }
    }

    when (state.dialog) {
        RegistrationRequestDialog.APPROVE -> ApproveDialog(
            fullName = state.request?.fullName.orEmpty(),
            isProcessing = state.isProcessing,
            onAction = onAction
        )
        RegistrationRequestDialog.REJECT -> RejectDialog(
            reason = state.rejectReason,
            isProcessing = state.isProcessing,
            onAction = onAction
        )
        null -> Unit
    }
}

@Composable
private fun RegistrationRequestDetailContent(
    request: RegistrationRequest,
    isProcessing: Boolean,
    onAction: (RegistrationRequestDetailAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(Icons.Default.Person, "ФИО", request.fullName.ifBlank { "—" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(Icons.Default.AccountCircle, "Логин", request.login)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(Icons.Default.Work, "Должность", request.position?.takeIf { it.isNotBlank() } ?: "Не указана")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(Icons.Default.Phone, "Телефон", request.phone?.takeIf { it.isNotBlank() } ?: "Не указан")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(Icons.Default.DateRange, "Дата подачи", formatDateTime(request.createdAt))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (request.status == RegStatusEnum.PENDING) {
            Button(
                onClick = { onAction(RegistrationRequestDetailAction.OnApproveClicked) },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Одобрить", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onAction(RegistrationRequestDetailAction.OnRejectClicked) },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Отклонить", fontSize = 16.sp)
            }
        } else {
            Text(
                text = "Заявка уже обработана: ${request.status.toDisplayString()}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ApproveDialog(
    fullName: String,
    isProcessing: Boolean,
    onAction: (RegistrationRequestDetailAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onAction(RegistrationRequestDetailAction.OnDialogDismissed) },
        title = { Text(text = "Одобрить регистрацию?") },
        text = {
            val who = if (fullName.isBlank()) "Пользователь" else "Пользователь $fullName"
            Text(text = "$who будет создан и сможет войти в систему.")
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(RegistrationRequestDetailAction.OnApproveConfirmed) },
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "Одобрить")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(RegistrationRequestDetailAction.OnDialogDismissed) },
                enabled = !isProcessing
            ) {
                Text(text = "Отмена")
            }
        }
    )
}

@Composable
private fun RejectDialog(
    reason: String,
    isProcessing: Boolean,
    onAction: (RegistrationRequestDetailAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onAction(RegistrationRequestDetailAction.OnDialogDismissed) },
        title = { Text(text = "Отклонить заявку") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { onAction(RegistrationRequestDetailAction.OnRejectReasonChanged(it)) },
                label = { Text(text = "Причина (обязательно)") },
                enabled = !isProcessing,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(RegistrationRequestDetailAction.OnRejectConfirmed) },
                enabled = !isProcessing && reason.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "Отклонить")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(RegistrationRequestDetailAction.OnDialogDismissed) },
                enabled = !isProcessing
            ) {
                Text(text = "Отмена")
            }
        }
    )
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

@Preview(showBackground = true)
@Composable
private fun RegistrationRequestDetailScreenPreview() {
    RegistrationRequestDetailScreen(
        state = RegistrationRequestDetailState(
            isLoading = false,
            request = RegistrationRequest(
                id = "1",
                login = "user_01",
                lastName = "Иванов",
                firstName = "Иван",
                middleName = "Иванович",
                position = "Инженер-механик",
                phone = "+375291234567"
            )
        ),
        onAction = {}
    )
}
