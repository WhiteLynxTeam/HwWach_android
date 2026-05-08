package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitelynxteam.hwwach.domain.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.NavigateToChangePassword -> {
                    // Navigate to change password screen
                }
                is ProfileEvent.NavigateToLogin -> {
                    // Navigate to login
                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        state.user?.let { user ->
            ProfileContent(
                user = user,
                modifier = modifier,
                onAction = viewModel::handleAction
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
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Телефон",
                    value = user.phone?.takeIf { it.isNotBlank() } ?: "Не указан"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.Work,
                    label = "Офис",
                    value = user.officeName?.takeIf { it.isNotBlank() } ?: "Не указан"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                ProfileInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Расположение офиса",
                    value = user.officeLocation?.takeIf { it.isNotBlank() } ?: "Не указано"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Change Password Button
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