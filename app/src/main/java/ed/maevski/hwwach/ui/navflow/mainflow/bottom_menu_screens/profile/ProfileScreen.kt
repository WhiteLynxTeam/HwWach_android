package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.domain.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        state.user?.let { user ->
            ProfileContent(
                user = user,
                isAdmin = state.isAdmin,
                molRequestMessage = state.molRequestMessage,
                modifier = modifier,
                onAction = onAction
            )
        } ?: run {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Данные пользователя недоступны", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    isAdmin: Boolean,
    molRequestMessage: String = "",
    modifier: Modifier = Modifier,
    onAction: (ProfileAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Аватар профиля",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Full Name
        val displayName = user.fullName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(user.lastName, user.firstName, user.middleName).joinToString(" ").takeIf { it.isNotBlank() }
            ?: "Неизвестный пользователь"

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        user.position?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(
                    icon = Icons.Default.AccountCircle,
                    label = "Логин",
                    value = user.username ?: "-",
                    isReadOnly = true
                )

                //HorizontalDivider поменять
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Телефон",
                    value = user.phone?.takeIf { it.isNotBlank() } ?: "Не указан"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.Work,
                    label = "Организация",
                    value = user.organizationName?.takeIf { it.isNotBlank() } ?: "Не указана"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileInfoRow(
                    icon = Icons.Default.AdminPanelSettings,
                    label = "Роль",
                    value = user.roleName?.takeIf { it.isNotBlank() } ?: "—"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // МОЛ — материально ответственное лицо: за ним могут числиться активы. Признак ставит админ
                ProfileInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Материально ответственное лицо",
                    value = when {
                        user.isMol -> "Да"
                        user.molRequested -> "Просьба на рассмотрении"
                        else -> "Нет"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // что за мной числится и с чем я работаю — подтвердить или отказаться
        OutlinedButton(
            onClick = { onAction(ProfileAction.OnAssignmentsClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Мне назначено", fontSize = 16.sp)
        }

        if (!user.isMol && !user.molRequested) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { onAction(ProfileAction.OnRequestMolClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Я — МОЛ (попросить признак)", fontSize = 16.sp)
            }
        }
        if (molRequestMessage.isNotBlank()) {
            Text(
                text = molRequestMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAdmin) {
            OutlinedButton(
                onClick = { onAction(ProfileAction.OnAdminClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Администрирование", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onAction(ProfileAction.OnChangePasswordClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Сменить пароль", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { onAction(ProfileAction.OnLogoutClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Выход из аккаунта", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Member Since Date
        val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(user.createdAt))
        Text(
            text = "Дата регистрации: $formattedDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isReadOnly: Boolean = false
) {
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
                color = if (isReadOnly) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}