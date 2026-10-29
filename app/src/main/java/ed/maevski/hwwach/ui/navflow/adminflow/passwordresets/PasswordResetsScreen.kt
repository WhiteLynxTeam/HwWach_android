package ed.maevski.hwwach.ui.navflow.adminflow.passwordresets

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.domain.models.PasswordResetRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetsScreen(
    modifier: Modifier = Modifier,
    state: PasswordResetsState,
    onAction: (PasswordResetsAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // сверху отступ даёт TopAppBar, снизу — кнопки навигации и клавиатура (edge-to-edge)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
    ) {
        TopAppBar(
            title = { Text(text = "Сброс пароля") },
            navigationIcon = {
                IconButton(onClick = { onAction(PasswordResetsAction.OnBackPressed) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            actions = {
                IconButton(onClick = { onAction(PasswordResetsAction.OnRefresh) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                }
            }
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(PasswordResetsAction.OnRefresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.errorMessage != null && state.requests.isEmpty() -> {
                    CenteredMessage(
                        title = "Не удалось загрузить заявки",
                        hint = state.errorMessage,
                        buttonText = "Повторить",
                        onButtonClick = { onAction(PasswordResetsAction.OnRefresh) }
                    )
                }
                state.requests.isEmpty() -> {
                    CenteredMessage(
                        title = "Нет заявок",
                        hint = "Заявки появляются, когда пользователь нажимает «Забыли пароль?» на экране входа"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.requests, key = { it.id }) { request ->
                            PasswordResetItem(
                                request = request,
                                isProcessing = state.processingId == request.id,
                                enabled = state.processingId == null,
                                onReset = { onAction(PasswordResetsAction.OnResetClicked(request)) },
                                onReject = { onAction(PasswordResetsAction.OnRejectClicked(request)) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.resetTarget?.let { target ->
        ResetDialog(
            target = target,
            password = state.passwordInput,
            error = state.dialogError,
            isProcessing = state.processingId == target.id,
            onAction = onAction
        )
    }

    state.done?.let { done ->
        DoneDialog(done = done, onDismiss = { onAction(PasswordResetsAction.OnDismissDialog) })
    }
}

@Composable
private fun PasswordResetItem(
    request: PasswordResetRequest,
    isProcessing: Boolean,
    enabled: Boolean,
    onReset: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.fullName.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${request.login}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatShortDate(request.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                OutlinedButton(onClick = onReject, enabled = enabled) { Text(text = "Отклонить") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onReset, enabled = enabled) { Text(text = "Сбросить") }
            }
        }
    }
}

@Composable
private fun ResetDialog(
    target: PasswordResetRequest,
    password: String,
    error: String?,
    isProcessing: Boolean,
    onAction: (PasswordResetsAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onAction(PasswordResetsAction.OnDismissDialog) },
        title = { Text(text = "Сброс пароля") },
        text = {
            Column {
                Text(text = "${target.fullName.ifBlank { target.login }} (@${target.login})")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { onAction(PasswordResetsAction.InputPassword(it)) },
                    label = { Text(text = "Временный пароль") },
                    placeholder = { Text(text = "Пусто — придумает сервер") },
                    singleLine = true,
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Старый пароль перестанет работать, сессии пользователя закончатся. При входе с временным паролем его попросят задать свой.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(PasswordResetsAction.OnConfirmReset) }, enabled = !isProcessing) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = "Сбросить")
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(PasswordResetsAction.OnDismissDialog) }, enabled = !isProcessing) {
                Text(text = "Отмена")
            }
        }
    )
}

@Composable
private fun DoneDialog(done: PasswordResetDone, onDismiss: () -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Пароль сброшен") },
        text = {
            Column {
                Text(text = "Передайте пользователю @${done.login} временный пароль:")
                Spacer(modifier = Modifier.height(12.dp))
                if (done.tempPassword != null) {
                    SelectionContainer {
                        Text(
                            text = done.tempPassword,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 22.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (done.isDefault) {
                    Text(text = "Установлен общий пароль по умолчанию (задан на сервере).")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Пароль больше нигде не покажется — запишите или скопируйте его сейчас.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = "Готово") }
        },
        dismissButton = {
            if (done.tempPassword != null) {
                TextButton(onClick = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("password", done.tempPassword)))
                    }
                }) { Text(text = "Копировать") }
            }
        }
    )
}

// verticalScroll нужен, чтобы pull-to-refresh срабатывал и на пустом экране
@Composable
private fun CenteredMessage(
    title: String,
    hint: String,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (buttonText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onButtonClick) { Text(text = buttonText) }
        }
    }
}

private fun formatShortDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(timestamp))

@Preview(showBackground = true)
@Composable
private fun PasswordResetsScreenPreview() {
    PasswordResetsScreen(
        state = PasswordResetsState(
            isLoading = false,
            requests = listOf(
                PasswordResetRequest(id = "1", login = "user_01", lastName = "Иванов", firstName = "Иван"),
                PasswordResetRequest(id = "2", login = "petrov", lastName = "Петров", firstName = "Пётр")
            )
        ),
        onAction = {}
    )
}
