package com.whitelynxteam.hwwach.ui.navflow.mainflow.assetdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whitelynxteam.hwwach.domain.models.Asset
import com.whitelynxteam.hwwach.ui.components.ImageGallery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    state: AssetDetailScreenState,
    onAction: (AssetDetailScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Инвентарная карточка") },
                navigationIcon = {
                    IconButton(onClick = { onAction(AssetDetailScreenAction.OnBackPressed) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(AssetDetailScreenAction.EditClicked) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = { onAction(AssetDetailScreenAction.DeleteClicked) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.asset == null) {
                Text(
                    text = "Актив не найден",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AssetInfoCard(asset = state.asset)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Фотографии",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (state.photos.isEmpty()) {
                        Text(
                            text = "Нет прикрепленных фотографий",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Переиспользуем ImageGallery в read-only режиме
                        ImageGallery(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            photos = state.photos,
                            onImageClick = { clientId -> onAction(AssetDetailScreenAction.PhotoClicked(clientId)) },
                            onDeleteClick = { /* Disabled */ },
                            showSourceSelector = { /* Disabled */ },
                            enabled = false // Отключаем удаление и добавление
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetInfoCard(asset: Asset, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DetailRow(label = "Наименование", value = asset.name)
            DetailRow(label = "Категория", value = asset.category ?: "Не указана")
            DetailRow(label = "Инв. номер", value = asset.inventoryNum ?: "Не указан")
            DetailRow(label = "Описание", value = asset.description ?: "Нет описания")
            val statusDisplay = when (asset.assetStatus) {
                com.whitelynxteam.hwwach.domain.models.AssetStatusEnum.ACTIVE -> "В эксплуатации"
                com.whitelynxteam.hwwach.domain.models.AssetStatusEnum.INACTIVE -> "Не используется"
                com.whitelynxteam.hwwach.domain.models.AssetStatusEnum.MAINTENANCE -> "На обслуживании"
                com.whitelynxteam.hwwach.domain.models.AssetStatusEnum.REPAIR -> "В ремонте"
                com.whitelynxteam.hwwach.domain.models.AssetStatusEnum.DECOMMISSIONED -> "Списан"
                com.whitelynxteam.hwwach.domain.models.AssetStatusEnum.LOST -> "Утерян"
                null -> "Не указан"
            }
            DetailRow(label = "Статус", value = statusDisplay)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
