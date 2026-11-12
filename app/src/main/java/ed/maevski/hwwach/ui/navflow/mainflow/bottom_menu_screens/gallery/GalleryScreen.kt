package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.gallery

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ed.maevski.hwwach.ui.components.ImageGallery
import ed.maevski.hwwach.ui.navflow.mainflow.addasset.CompactPickerRow
import ed.maevski.hwwach.ui.navflow.mainflow.addasset.LocationPickerBody
import ed.maevski.hwwach.ui.theme.Blue800
import ed.maevski.hwwach.ui.theme.Gray250

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    state: GalleryScreenState,
    onAction: (GalleryScreenAction) -> Unit,
    onNavigateToCamera: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri -> onAction(GalleryScreenAction.AddImage(uri.toString())) }
    }

    val sheetState = rememberModalBottomSheetState()

    val localOnAction = remember(onAction, onNavigateToCamera) {
        { action: GalleryScreenAction ->
            when (action) {
                GalleryScreenAction.ShowSourceSelector -> showSourceSheet = true
                GalleryScreenAction.OpenGallery -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                GalleryScreenAction.OpenCamera -> {
                    onAction(action)
                    onNavigateToCamera()
                }
                else -> onAction(action)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray250),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.photos.isEmpty() && state.groups.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Информация",
                            tint = Blue800,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Сфотографируйте оборудование для инвентаризации. Много снимков — заведите группу " +
                                "(место или человек): новые снимки попадут в неё",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (state.selectionMode) {
                SelectionBar(state, localOnAction)
            } else {
                GroupChipsRow(state, localOnAction)
                state.activeGroup?.let { group ->
                    GroupHeader(
                        name = group.name,
                        subtitle = group.subtitle,
                        note = group.note,
                        onEdit = { localOnAction(GalleryScreenAction.EditGroup(group.id)) },
                    )
                }
            }

            if (state.canUpload || state.isUploading || state.isServerSyncing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAction(GalleryScreenAction.SyncPendingPhotos) },
                        enabled = !state.isUploading && !state.isServerSyncing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (state.isUploading || state.isServerSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                        Text(
                            when {
                                state.isServerSyncing -> "Синхронизация..."
                                state.isUploading -> "Отправка..."
                                else -> "Отправить фото"
                            }
                        )
                    }

                    if (state.isUploading) {
                        TextButton(
                            onClick = { onAction(GalleryScreenAction.CancelSync) },
                        ) {
                            Text("Отмена", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (state.uploadError.isNotEmpty()) {
                Text(
                    text = state.uploadError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (state.errorMessage.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (state.notice.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.notice,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onAction(GalleryScreenAction.DismissNotice) }) {
                        Icon(Icons.Default.Close, contentDescription = "Скрыть")
                    }
                }
            }

            if (state.photos.isNotEmpty() && state.visiblePhotos.isEmpty() && state.activeGroup == null) {
                Text(
                    "Здесь снимков нет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            ImageGallery(
                modifier = Modifier.weight(1f),
                photos = state.visiblePhotos,
                onImageClick = { id -> localOnAction(GalleryScreenAction.PhotoClick(id)) },
                onDeleteClick = { photo -> localOnAction(GalleryScreenAction.RemovePhoto(photo)) },
                showSourceSelector = { localOnAction(GalleryScreenAction.ShowSourceSelector) },
                enabled = !state.isUploading && !state.isServerSyncing,
                selectionMode = state.selectionMode,
                selectedIds = state.selectedIds,
                onLongClick = { photo -> localOnAction(GalleryScreenAction.PhotoLongClick(photo.clientId)) },
                showNotes = true,
            )
        }
    }

    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Добавить фото",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)
                )
                Text(
                    text = state.activeGroup?.let { "В группу «${it.name}»" } ?: "Без группы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Сделать снимок") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = "Камера") },
                    modifier = Modifier
                        .clickable {
                            showSourceSheet = false
                            localOnAction(GalleryScreenAction.OpenCamera)
                        }
                        .padding(horizontal = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Выбрать из галереи") },
                    leadingContent = { Icon(Icons.Default.Image, contentDescription = "Галерея") },
                    modifier = Modifier
                        .clickable {
                            showSourceSheet = false
                            localOnAction(GalleryScreenAction.OpenGallery)
                        }
                        .padding(horizontal = 12.dp)
                )
            }
        }
    }

    if (state.moveSheetOpen) MoveSheet(state, localOnAction)
    state.noteEditor?.let { NoteDialog(it, localOnAction) }
    state.groupEditor?.let { GroupEditorSheet(state, it, localOnAction) }
}

// ===== Группы: строка фильтров =====

@Composable
private fun GroupChipsRow(state: GalleryScreenState, onAction: (GalleryScreenAction) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "all") {
            FilterChip(
                selected = state.filter == GalleryFilter.All,
                onClick = { onAction(GalleryScreenAction.SelectFilter(GalleryFilter.All)) },
                label = { Text("Все · ${state.photos.size}") },
            )
        }
        item(key = "ungrouped") {
            FilterChip(
                selected = state.filter == GalleryFilter.Ungrouped,
                onClick = { onAction(GalleryScreenAction.SelectFilter(GalleryFilter.Ungrouped)) },
                label = { Text("Без группы · ${state.ungroupedCount}") },

            )
        }
        items(state.groups, key = { it.group.id }) { chip ->
            FilterChip(
                selected = state.filter == GalleryFilter.Group(chip.group.id),
                onClick = { onAction(GalleryScreenAction.SelectFilter(GalleryFilter.Group(chip.group.id))) },
                label = { Text("${chip.group.name} · ${chip.count}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Default.Folder, null, Modifier.size(16.dp)) },
            )
        }
        item(key = "new") {
            AssistChip(
                onClick = { onAction(GalleryScreenAction.NewGroup) },
                label = { Text("Группа") },
                leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(AssistChipDefaults.IconSize)) },
            )
        }
        item(key = "hide") {
            FilterChip(
                selected = state.hideAttached,
                onClick = { onAction(GalleryScreenAction.ToggleHideAttached) },
                label = { Text("Скрыть уже в активах") },
            )
        }
    }
}

@Composable
private fun GroupHeader(name: String, subtitle: String, note: String?, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (!note.isNullOrBlank()) {
                    Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "Новые снимки попадают в эту группу. Долгое нажатие — выбрать снимки",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Изменить группу") }
        }
    }
}

// ===== Выбор снимков =====

@Composable
private fun SelectionBar(state: GalleryScreenState, onAction: (GalleryScreenAction) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(Modifier.padding(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onAction(GalleryScreenAction.ClearSelection) }) {
                    Icon(Icons.Default.Close, contentDescription = "Снять выбор")
                }
                Text("Выбрано: ${state.selectedIds.size}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { onAction(GalleryScreenAction.SelectAllVisible) }) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Выбрать все")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SelectionButton(Icons.Default.AddBox, "Актив") { onAction(GalleryScreenAction.CreateAssetFromSelected) }
                SelectionButton(Icons.Default.FolderOpen, "В группу") { onAction(GalleryScreenAction.OpenMoveSheet) }
                SelectionButton(Icons.Default.Edit, "Подпись") { onAction(GalleryScreenAction.OpenNoteEditor) }
                SelectionButton(Icons.Default.Delete, "Удалить") { onAction(GalleryScreenAction.DeleteSelected) }
            }
        }
    }
}

@Composable
private fun SelectionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveSheet(state: GalleryScreenState, onAction: (GalleryScreenAction) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onAction(GalleryScreenAction.CloseMoveSheet) }) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                "Перенести ${state.selectedIds.size} снимк. в группу",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                item(key = "new") {
                    ListItem(
                        headlineContent = { Text("Новая группа…", color = MaterialTheme.colorScheme.primary) },
                        leadingContent = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable { onAction(GalleryScreenAction.MoveSelectedToNewGroup) },
                    )
                }
                items(state.groups, key = { it.group.id }) { chip ->
                    ListItem(
                        headlineContent = { Text(chip.group.name) },
                        supportingContent = if (chip.group.subtitle.isNotEmpty()) {
                            { Text(chip.group.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        } else {
                            null
                        },
                        leadingContent = { Icon(Icons.Default.Folder, null) },
                        trailingContent = { Text("${chip.count}") },
                        modifier = Modifier.clickable { onAction(GalleryScreenAction.MoveSelectedTo(chip.group.id)) },
                    )
                }
                item(key = "none") {
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Убрать из группы") },
                        leadingContent = { Icon(Icons.Default.Close, null) },
                        modifier = Modifier.clickable { onAction(GalleryScreenAction.MoveSelectedTo(null)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteDialog(editor: NoteEditorState, onAction: (GalleryScreenAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(GalleryScreenAction.CloseNoteEditor) },
        title = { Text(if (editor.photoIds.size == 1) "Подпись снимка" else "Подпись ${editor.photoIds.size} снимков") },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editor.text,
                onValueChange = { onAction(GalleryScreenAction.NoteInput(it)) },
                placeholder = { Text("Например: монитор, инв. 1234, шильдик") },
                shape = RoundedCornerShape(16.dp),
                maxLines = 4,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAction(GalleryScreenAction.SaveNote) }) {
                Text(if (editor.text.isBlank()) "Убрать подпись" else "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(GalleryScreenAction.CloseNoteEditor) }) { Text("Отмена") }
        },
    )
}

// ===== Редактор группы: название, место, человек, заметка =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditorSheet(state: GalleryScreenState, editor: GroupEditorState, onAction: (GalleryScreenAction) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { onAction(GalleryScreenAction.CloseGroupEditor) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            when (editor.step) {
                GroupEditorStep.MAIN -> GroupMainStep(editor, onAction)
                GroupEditorStep.PLACE -> {
                    BackRow("К группе") { onAction(GalleryScreenAction.GroupBack) }
                    LocationPickerBody(
                        title = "Место группы",
                        sheet = editor.placeSheet,
                        allLocations = state.allLocations,
                        recentLocations = state.recentLocations,
                        onKind = { onAction(GalleryScreenAction.GroupLocKind(it)) },
                        onQuery = { onAction(GalleryScreenAction.GroupLocQuery(it)) },
                        onPick = { onAction(GalleryScreenAction.GroupLocPick(it)) },
                        onAddNew = { onAction(GalleryScreenAction.GroupLocAddNew(it)) },
                        onCutTo = { onAction(GalleryScreenAction.GroupLocCutTo(it)) },
                        onConfirm = { onAction(GalleryScreenAction.GroupLocConfirm) },
                    )
                    TextButton(onClick = { onAction(GalleryScreenAction.GroupLocClear) }) { Text("Без места") }
                }
                GroupEditorStep.PERSON -> GroupPersonStep(state, editor, onAction)
            }
        }
    }
}

@Composable
private fun BackRow(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun GroupMainStep(editor: GroupEditorState, onAction: (GalleryScreenAction) -> Unit) {
    Text(if (editor.id == null) "Новая группа" else "Группа", style = MaterialTheme.typography.titleLarge)
    Text(
        "Место и человек подставятся в активы, созданные из снимков группы",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = editor.name,
        onValueChange = { onAction(GalleryScreenAction.GroupName(it)) },
        label = { Text("Название") },
        placeholder = { Text("Ферма 700, кабинет зав. фермы") },
        singleLine = true,
        isError = editor.error.isNotEmpty(),
        supportingText = if (editor.error.isNotEmpty()) {
            { Text(editor.error) }
        } else {
            null
        },
        shape = RoundedCornerShape(16.dp),
    )
    Spacer(Modifier.height(8.dp))
    CompactPickerRow(
        label = "Место",
        value = editor.placeText,
        icon = Icons.Default.Place,
        pinned = false,
        onClick = { onAction(GalleryScreenAction.GroupOpenPlace) },
        onTogglePin = {},
        showPin = false,
    )
    Spacer(Modifier.height(8.dp))
    CompactPickerRow(
        label = "Человек",
        value = editor.person?.fullName ?: "Не указан",
        icon = Icons.Default.Person,
        pinned = false,
        onClick = { onAction(GalleryScreenAction.GroupOpenPerson) },
        onTogglePin = {},
        showPin = false,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = editor.note,
        onValueChange = { onAction(GalleryScreenAction.GroupNote(it)) },
        label = { Text("Заметка") },
        maxLines = 3,
        shape = RoundedCornerShape(16.dp),
    )
    Spacer(Modifier.height(12.dp))
    Button(modifier = Modifier.fillMaxWidth(), onClick = { onAction(GalleryScreenAction.SaveGroup) }) {
        Text(
            when {
                editor.id == null && editor.moveSelected -> "Создать и перенести снимки"
                editor.id == null -> "Создать и снимать в неё"
                else -> "Сохранить"
            }
        )
    }
    if (editor.id != null) {
        TextButton(modifier = Modifier.fillMaxWidth(), onClick = { onAction(GalleryScreenAction.DeleteGroup) }) {
            Text("Удалить группу (снимки останутся)", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun GroupPersonStep(state: GalleryScreenState, editor: GroupEditorState, onAction: (GalleryScreenAction) -> Unit) {
    BackRow("К группе") { onAction(GalleryScreenAction.GroupBack) }
    Text("Человек группы", style = MaterialTheme.typography.titleLarge)
    Text(
        "Если у человека признак МОЛ, он станет МОЛ активов из этой группы",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = editor.personQuery,
        onValueChange = { onAction(GalleryScreenAction.GroupPersonQuery(it)) },
        placeholder = { Text("Поиск по ФИО") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
    val q = editor.personQuery.trim().lowercase()
    val list = if (q.isEmpty()) state.persons else state.persons.filter { it.fullName.lowercase().contains(q) }
    LazyColumn(Modifier.heightIn(max = 380.dp)) {
        item(key = "none") {
            ListItem(
                headlineContent = { Text("Без человека") },
                modifier = Modifier.clickable { onAction(GalleryScreenAction.GroupPersonPick(null)) },
            )
            HorizontalDivider()
        }
        items(list, key = { it.id }) { p ->
            ListItem(
                headlineContent = { Text(p.fullName) },
                supportingContent = {
                    Text(listOfNotNull(p.position, if (p.isMol) "МОЛ" else null).joinToString(" · ").ifEmpty { " " })
                },
                modifier = Modifier.clickable { onAction(GalleryScreenAction.GroupPersonPick(p)) },
            )
        }
        if (state.personsError.isNotEmpty()) {
            item(key = "err") {
                Text(
                    state.personsError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddScreenPreview() {
    GalleryScreen(
        state = GalleryScreenState(),
        onAction = {}
    )
}
