package ed.maevski.hwwach.ui.navflow.adminflow.hub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import ed.maevski.hwwach.domain.models.OrgNumber
import ed.maevski.hwwach.ui.common.OrgInvite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
    modifier: Modifier = Modifier,
    state: AdminHubState,
    onAction: (AdminHubAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // сверху отступ даёт TopAppBar, снизу — кнопки навигации и клавиатура (edge-to-edge)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
    ) {
        TopAppBar(
            title = { Text(text = "Администрирование") },
            navigationIcon = {
                IconButton(onClick = { onAction(AdminHubAction.OnBackPressed) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            AdminHubItem(
                icon = Icons.Default.PersonAdd,
                title = "Заявки на регистрацию",
                badgeCount = state.pendingRegistrationsCount,
                onClick = { onAction(AdminHubAction.OnRegistrationRequestsClicked) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdminHubItem(
                icon = Icons.Default.LockReset,
                title = "Сброс пароля",
                badgeCount = state.pendingPasswordResetsCount,
                onClick = { onAction(AdminHubAction.OnPasswordResetsClicked) }
            )
            state.organizationNumber?.takeIf { it.isNotBlank() }?.let { number ->
                Spacer(modifier = Modifier.height(16.dp))
                InviteCard(number = number, organizationName = state.organizationName)
            }
        }
    }
}

/** Номер организации и ссылка-приглашение: сотрудник открывает QR камерой — номер подставится на входе. */
@Composable
private fun InviteCard(number: String, organizationName: String?) {
    val context = LocalContext.current
    var showQr by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Пригласить сотрудника", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Номер организации",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = OrgNumber.format(number), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                TextButton(onClick = { OrgInvite.copy(context, number) }) { Text("Копировать") }
                TextButton(onClick = { OrgInvite.share(context, number, organizationName) }) { Text("Поделиться") }
                TextButton(onClick = { showQr = true }) { Text("QR-код") }
            }
        }
    }
    if (showQr) {
        val qr = remember(number) { OrgInvite.qr(number).asImageBitmap() }
        AlertDialog(
            onDismissRequest = { showQr = false },
            title = { Text("Вход в «${organizationName ?: OrgNumber.format(number)}»") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = qr,
                        contentDescription = "QR-код ссылки для входа",
                        modifier = Modifier.size(240.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Наведите камеру телефона: откроется страница, с неё — приложение с номером " +
                            OrgNumber.format(number) + ".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showQr = false }) { Text("Готово") } }
        )
    }
}

@Composable
private fun AdminHubItem(
    icon: ImageVector,
    title: String,
    badgeCount: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (badgeCount != null && badgeCount > 0) {
                Badge { Text(text = badgeCount.toString()) }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminHubScreenPreview() {
    AdminHubScreen(
        state = AdminHubState(pendingRegistrationsCount = 3, pendingPasswordResetsCount = 1),
        onAction = {}
    )
}
