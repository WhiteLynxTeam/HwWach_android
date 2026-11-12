package ed.maevski.hwwach.ui.navflow.mainflow.addasset

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ed.maevski.hwwach.common.extensions.PriorityAsyncImage
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.LocationKind
import ed.maevski.hwwach.domain.models.MolMode
import ed.maevski.hwwach.domain.models.UploadQueueState

// ===== Строка формы: значение одной строкой + закрепить =====

@Composable
fun CompactPickerRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    pinned: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    showPin: Boolean = true,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (showPin) {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (pinned) "Открепить" else "Закрепить для следующих активов",
                        tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun LocationRow(state: AddAssetScreenState, onAction: (AddAssetScreenAction) -> Unit) {
    CompactPickerRow(
        label = if (state.locationPinned) "Место · закреплено" else "Место",
        value = state.locationText,
        icon = Icons.Default.Place,
        pinned = state.locationPinned,
        onClick = { onAction(AddAssetScreenAction.OpenLocation) },
        onTogglePin = { onAction(AddAssetScreenAction.TogglePinLocation) },
        showPin = !state.isEdit,
    )
}

@Composable
fun MolRow(state: AddAssetScreenState, onAction: (AddAssetScreenAction) -> Unit) {
    CompactPickerRow(
        label = if (state.molPinned) "МОЛ · закреплён" else "МОЛ",
        value = state.molText,
        icon = Icons.Default.Person,
        pinned = state.molPinned,
        onClick = { onAction(AddAssetScreenAction.OpenMol) },
        onTogglePin = { onAction(AddAssetScreenAction.TogglePinMol) },
    )
}

// ===== Очередь отправки =====

@Composable
fun UploadQueueCard(state: AddAssetScreenState, onAction: (AddAssetScreenAction) -> Unit) {
    val q: UploadQueueState = state.queue
    if (q.isEmpty && state.savedInSession == 0) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (q.failed.isNotEmpty()) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    q.queued > 0 -> CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    q.failed.isNotEmpty() -> Icon(Icons.Default.ErrorOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    else -> Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(8.dp))
                val parts = buildList {
                    if (state.savedInSession > 0) add("Сохранено: ${state.savedInSession}")
                    if (q.queued > 0) add("отправляется: ${q.queued}")
                    if (q.failed.isNotEmpty()) add("не ушло: ${q.failed.size}")
                    if (q.queued == 0 && q.failed.isEmpty()) add("всё отправлено")
                }
                Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                if (q.failed.isNotEmpty()) {
                    TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Скрыть" else "Что не так") }
                }
            }
            if (expanded && q.failed.isNotEmpty()) {
                q.failed.forEach { f ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onAction(AddAssetScreenAction.OpenFailed(f.clientId)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(f.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(f.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Исправить")
                    }
                }
                TextButton(onClick = { onAction(AddAssetScreenAction.RetryFailed) }) {
                    Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Отправить снова")
                }
            }
        }
    }
}

// ===== Выбор места =====

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LocationSheet(state: AddAssetScreenState, onAction: (AddAssetScreenAction) -> Unit) {
    val sheet = state.locationSheet ?: return
    ModalBottomSheet(
        onDismissRequest = { onAction(AddAssetScreenAction.CloseLocation) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            LocationPickerBody(
                title = "Где находится",
                sheet = sheet,
                allLocations = state.allLocations,
                recentLocations = state.recentLocations,
                onKind = { onAction(AddAssetScreenAction.LocationSetKind(it)) },
                onQuery = { onAction(AddAssetScreenAction.LocationQuery(it)) },
                onPick = { onAction(AddAssetScreenAction.LocationPick(it)) },
                onAddNew = { onAction(AddAssetScreenAction.LocationAddNew(it)) },
                onCutTo = { onAction(AddAssetScreenAction.LocationCutTo(it)) },
                onConfirm = { onAction(AddAssetScreenAction.LocationConfirm) },
            )
        }
    }
}

/** Выбор места: вид (место / носимое / без места) и цепочка по справочнику. Общий для формы актива и группы снимков */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LocationPickerBody(
    title: String,
    sheet: LocationSheetState,
    allLocations: List<Location>,
    recentLocations: List<Location>,
    onKind: (AssetLocationKind) -> Unit,
    onQuery: (String) -> Unit,
    onPick: (Location) -> Unit,
    onAddNew: (String) -> Unit,
    onCutTo: (Int) -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Готово",
) {
    Column {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(AssetLocationKind.PLACE, AssetLocationKind.PORTABLE, AssetLocationKind.NONE).forEach { kind ->
                    FilterChip(
                        selected = sheet.kind == kind,
                        onClick = { onKind(kind) },
                        label = {
                            Text(
                                when (kind) {
                                    AssetLocationKind.PLACE -> "Место"
                                    AssetLocationKind.PORTABLE -> "Носимое"
                                    else -> "Без места"
                                }
                            )
                        },
                    )
                }
            }

            if (sheet.kind != AssetLocationKind.PLACE) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (sheet.kind == AssetLocationKind.PORTABLE) {
                        "Смартфоны, ЭЦП, ноутбуки — место «у сотрудника»: у МОЛ или работающего с активом"
                    } else {
                        "Почтовые ящики, лицензии, учётные записи — физического места нет"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                PlacePicker(sheet, allLocations, recentLocations, onQuery, onPick, onAddNew, onCutTo)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onConfirm,
            ) { Text(confirmText) }
    }
}

@Composable
private fun PlacePicker(
    sheet: LocationSheetState,
    allLocations: List<Location>,
    recentLocations: List<Location>,
    onQuery: (String) -> Unit,
    onPick: (Location) -> Unit,
    onAddNew: (String) -> Unit,
    onCutTo: (Int) -> Unit,
) {
    val last = sheet.chain.lastOrNull()
    val query = sheet.query.trim()
    val q = query.lowercase()

    // дети выбранного уровня, по вводу — ещё совпадения по пути из любого места справочника
    val children = allLocations.filter { it.parentId == last?.id && (q.isEmpty() || it.name.lowercase().contains(q)) }
    val elsewhere = if (q.length >= 2) {
        allLocations.filter { it.parentId != last?.id && it.path.lowercase().contains(q) }.take(15)
    } else {
        emptyList()
    }
    val exactExists = children.any { it.name.equals(query, ignoreCase = true) }

    // выбранная цепочка: каждый уровень можно сбросить
    sheet.chain.forEachIndexed { index, loc ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(LocationKind.forDepth(index).title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(loc.name + if (!loc.synced) "  · новое" else "", style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = { onCutTo(index) }) {
                Icon(Icons.Default.Close, contentDescription = "Убрать уровень")
            }
        }
    }

    if (sheet.chain.isEmpty() && recentLocations.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.width(4.dp))
            Text("Недавние", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recentLocations, key = { it.id }) { loc ->
                AssistChip(
                    onClick = { onPick(loc) },
                    label = { Text(loc.path.replace(" / ", " › "), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = sheet.query,
        onValueChange = onQuery,
        label = { Text(LocationKind.forDepth(sheet.chain.size).title + if (sheet.chain.isEmpty()) "" else " (необязательно)") },
        placeholder = { Text("Найти или добавить") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )

    LazyColumn(Modifier.heightIn(max = 280.dp)) {
        if (query.isNotEmpty() && !exactExists) {
            item(key = "add") {
                ListItem(
                    headlineContent = { Text("Добавить «$query»", color = MaterialTheme.colorScheme.primary) },
                    supportingContent = if (last != null) {
                        { Text("в ${last.path.replace(" / ", " › ")}") }
                    } else {
                        null
                    },
                    leadingContent = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onAddNew(query) },
                )
            }
        }
        items(children, key = { "c" + it.id }) { loc ->
            ListItem(
                headlineContent = { Text(loc.name) },
                modifier = Modifier.clickable { onPick(loc) },
            )
        }
        if (elsewhere.isNotEmpty()) {
            item(key = "elsewhere") {
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text("В других местах", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }
            items(elsewhere, key = { "e" + it.id }) { loc ->
                ListItem(
                    headlineContent = { Text(loc.name) },
                    supportingContent = { Text(loc.path.replace(" / ", " › "), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.clickable { onPick(loc) },
                )
            }
        }
    }
}

// ===== Выбор МОЛ =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MolSheet(state: AddAssetScreenState, onAction: (AddAssetScreenAction) -> Unit) {
    if (!state.molSheetOpen) return
    ModalBottomSheet(
        onDismissRequest = { onAction(AddAssetScreenAction.CloseMol) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("МОЛ нового актива", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.molQuery,
                onValueChange = { onAction(AddAssetScreenAction.MolQuery(it)) },
                placeholder = { Text("Поиск по ФИО") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                item(key = "default") {
                    ListItem(
                        headlineContent = { Text("По умолчанию") },
                        supportingContent = { Text("Вы, если у вас признак МОЛ; иначе — без МОЛ") },
                        modifier = Modifier.clickable { onAction(AddAssetScreenAction.MolPick(MolMode.DEFAULT)) },
                    )
                }
                item(key = "none") {
                    ListItem(
                        headlineContent = { Text("Без МОЛ") },
                        supportingContent = { Text("Закрепить позже в карточке или при проверке") },
                        modifier = Modifier.clickable { onAction(AddAssetScreenAction.MolPick(MolMode.NONE)) },
                    )
                    HorizontalDivider()
                }
                items(state.molCandidates, key = { it.id }) { p ->
                    ListItem(
                        headlineContent = { Text(p.fullName) },
                        supportingContent = if (p.position != null) {
                            { Text(p.position) }
                        } else {
                            null
                        },
                        modifier = Modifier.clickable { onAction(AddAssetScreenAction.MolPick(MolMode.PERSON, p)) },
                    )
                }
                if (state.molCandidates.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "Людей с признаком МОЛ не найдено. Нового человека можно завести в карточке актива — «Назначить МОЛ»",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ===== Снимки приложения =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPhotosSheet(state: AddAssetScreenState, onAction: (AddAssetScreenAction) -> Unit) {
    if (!state.appPhotosOpen) return
    ModalBottomSheet(
        onDismissRequest = { onAction(AddAssetScreenAction.CloseAppPhotos) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("Снимки приложения", style = MaterialTheme.typography.titleLarge)
            Text(
                "Сделанные во вкладке «Галерея» и ещё не прикреплённые к активу",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
            if (state.appPhotoGroups.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "all") {
                        FilterChip(
                            selected = state.appPhotoGroupFilter == null,
                            onClick = { onAction(AddAssetScreenAction.AppPhotosGroupFilter(null)) },
                            label = { Text("Все") },
                        )
                    }
                    items(state.appPhotoGroups, key = { it.id }) { g ->
                        FilterChip(
                            selected = state.appPhotoGroupFilter == g.id,
                            onClick = { onAction(AddAssetScreenAction.AppPhotosGroupFilter(g.id)) },
                            label = { Text(g.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }
            val shown = state.appPhotoGroupFilter?.let { gid -> state.appPhotos.filter { it.groupId == gid } } ?: state.appPhotos
            Spacer(Modifier.height(8.dp))
            if (shown.isEmpty()) {
                Text("Свободных снимков нет", modifier = Modifier.padding(vertical = 24.dp))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(shown, key = { it.clientId }) { photo ->
                        val selected = photo.clientId in state.selectedAppPhotoIds
                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { onAction(AddAssetScreenAction.ToggleAppPhoto(photo.clientId)) },
                        ) {
                            PriorityAsyncImage(
                                photo = photo,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            val note = photo.note
                            if (!note.isNullOrBlank()) {
                                Text(
                                    note,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                )
                            }
                            if (selected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Выбрано",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .clip(CircleShape),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedAppPhotoIds.isNotEmpty(),
                onClick = { onAction(AddAssetScreenAction.ConfirmAppPhotos) },
            ) { Text("Добавить (${state.selectedAppPhotoIds.size})") }
        }
    }
}
