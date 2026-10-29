package ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ed.maevski.hwwach.domain.models.RegistrationRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationRequestsScreen(
    modifier: Modifier = Modifier,
    state: RegistrationRequestsState,
    onAction: (RegistrationRequestsAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // сверху отступ даёт TopAppBar, снизу — кнопки навигации и клавиатура (edge-to-edge)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
    ) {
        TopAppBar(
            title = { Text(text = "Заявки на регистрацию") },
            navigationIcon = {
                IconButton(onClick = { onAction(RegistrationRequestsAction.OnBackPressed) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            actions = {
                IconButton(onClick = { onAction(RegistrationRequestsAction.OnRefresh) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                }
            }
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(RegistrationRequestsAction.OnRefresh) },
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
                        onButtonClick = { onAction(RegistrationRequestsAction.OnRefresh) }
                    )
                }
                state.requests.isEmpty() -> {
                    CenteredMessage(
                        title = "Нет новых заявок",
                        hint = "Все заявки на регистрацию обработаны"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.requests, key = { it.id }) { request ->
                            RegistrationRequestItem(
                                request = request,
                                onClick = { onAction(RegistrationRequestsAction.OnRequestClicked(request.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationRequestItem(
    request: RegistrationRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
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
private fun RegistrationRequestsScreenPreview() {
    RegistrationRequestsScreen(
        state = RegistrationRequestsState(
            isLoading = false,
            requests = listOf(
                RegistrationRequest(id = "1", login = "user_01", lastName = "Иванов", firstName = "Иван", middleName = "Иванович"),
                RegistrationRequest(id = "2", login = "petrov", lastName = "Петров", firstName = "Пётр")
            )
        ),
        onAction = {}
    )
}
