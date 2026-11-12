package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import ed.maevski.hwwach.ui.theme.Gray250

@Composable
fun AssetsScreen(
    state: AssetsScreenState,
    onAction: (AssetsScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Gray250,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(AssetsScreenAction.AddAssetClicked) }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить актив")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item(key = "search") {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.query,
                            onValueChange = { onAction(AssetsScreenAction.Search(it)) },
                            placeholder = { Text("Поиск: название, номер, место") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (state.query.isNotEmpty()) {
                                    IconButton(onClick = { onAction(AssetsScreenAction.Search("")) }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                        )
                        if (state.errorMessage.isNotEmpty()) {
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (state.assets.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = if (state.query.isBlank()) "Нет активов" else "Ничего не найдено",
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                    items(state.assets, key = { it.clientId }) { asset ->
                        AssetItem(
                            asset = asset,
                            onClick = { onAction(AssetsScreenAction.AssetClicked(asset.clientId)) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AssetItem(
    asset: Asset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Наименование — главная надпись
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Категория — chip/badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!asset.category.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = asset.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!asset.inventoryNum.isNullOrBlank()) {
                        Text(
                            text = "№ ${asset.inventoryNum}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val place = when (asset.locationKind) {
                    AssetLocationKind.PLACE -> asset.locationPath?.replace(" / ", " › ")
                    AssetLocationKind.UNSET -> null
                    else -> asset.locationKind.title
                }
                if (place != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ещё не на сервере: ждёт сети или не прошёл (номер занят и т.п.)
                if (asset.status != UploadStatusEnum.SYNCED) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (asset.status == UploadStatusEnum.FAILED) {
                            "Не отправлен: " + (asset.errorMessage ?: "ошибка")
                        } else {
                            "Ждёт отправки"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (asset.status == UploadStatusEnum.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!asset.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = asset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Стрелка — индикатор перехода
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}