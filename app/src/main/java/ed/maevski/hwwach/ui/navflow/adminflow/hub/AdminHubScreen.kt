package ed.maevski.hwwach.ui.navflow.adminflow.hub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
    modifier: Modifier = Modifier,
    state: AdminHubState,
    onAction: (AdminHubAction) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
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
        }
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
        state = AdminHubState(pendingRegistrationsCount = 3),
        onAction = {}
    )
}
