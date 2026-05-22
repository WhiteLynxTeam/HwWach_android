package com.whitelynxteam.hwwach.ui.navflow.mainflow.addasset

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.whitelynxteam.hwwach.BuildConfig
import com.whitelynxteam.hwwach.ui.components.ImageGallery
import com.whitelynxteam.hwwach.ui.models.Categories
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    modifier: Modifier = Modifier,
    state: AddAssetScreenState,
    onAction: (AddAssetScreenAction) -> Unit
) {
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }
    var tempCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri -> onAction(AddAssetScreenAction.AddImage(uri.toString())) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUriString?.let { onAction(AddAssetScreenAction.AddImage(it)) }
        }
    }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val localOnAction = remember(onAction) {
        { action: AddAssetScreenAction ->
            when (action) {
                AddAssetScreenAction.ShowSourceSelector -> showSourceSheet = true
                AddAssetScreenAction.OpenGallery -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                AddAssetScreenAction.OpenCamera -> {
                    val photoFile = File(
                        context.externalCacheDir,
                        "HW_watch_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
                    )
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        photoFile
                    )
                    tempCameraUriString = uri.toString()
                    cameraLauncher.launch(uri)
                }
                else -> onAction(action)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Добавить актив") },
            navigationIcon = {
                IconButton(
                    onClick = { onAction(AddAssetScreenAction.NavigateBack) }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Column {
                    AddFormTextField(
                        value = state.name,
                        onValueChange = { onAction(AddAssetScreenAction.InputName(it)) },
                        label = "Название"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CategoryDropdown(
                        selectedCategory = state.category,
                        onCategorySelected = { onAction(AddAssetScreenAction.InputCategory(it)) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AddFormTextField(
                        value = state.inventoryNumber,
                        onValueChange = { onAction(AddAssetScreenAction.InputInventoryNumber(it)) },
                        label = "Инвентаризационный номер"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AddFormTextField(
                        value = state.address,
                        onValueChange = { onAction(AddAssetScreenAction.InputAddress(it)) },
                        label = "Адрес"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AddFormTextField(
                        value = state.comment,
                        onValueChange = { onAction(AddAssetScreenAction.InputComment(it)) },
                        label = "Комментарий"
                    )
                }
            }

            item {
                ImageGallery(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .heightIn(min = 120.dp, max = 320.dp),
                    photos = state.photos,
                    onImageClick = { clientId -> localOnAction(AddAssetScreenAction.OpenFullImage(clientId)) },
                    onDeleteClick = { photo -> localOnAction(AddAssetScreenAction.RemovePhoto(photo)) },
                    showSourceSelector = { localOnAction(AddAssetScreenAction.ShowSourceSelector) },
                    enabled = true
                )
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    onClick = {
                        onAction(AddAssetScreenAction.Submit)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    )
                ) {
                    Text(
                        text = "Добавить",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showSourceSheet) {
        androidx.compose.material3.ModalBottomSheet(
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
                    modifier = Modifier.padding(16.dp)
                )

                ListItem(
                    headlineContent = { Text("Сделать снимок") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = "Камера") },
                    modifier = Modifier
                        .clickable {
                            showSourceSheet = false
                            localOnAction(AddAssetScreenAction.OpenCamera)
                        }
                        .padding(horizontal = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Выбрать из галереи") },
                    leadingContent = { Icon(Icons.Default.Image, contentDescription = "Галерея") },
                    modifier = Modifier
                        .clickable {
                            showSourceSheet = false
                            localOnAction(AddAssetScreenAction.OpenGallery)
                        }
                        .padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun AddFormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val borderColor =
        if (isFocused || value.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            label = {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

// @Composable
// private fun ImageCard(
//     modifier: Modifier = Modifier,
//     photo: Photo,
// ) {
//     PriorityAsyncImage(
//         photo = photo,
//         contentDescription = "Image",
//         modifier = modifier
//             .clip(RoundedCornerShape(16.dp)),
//         contentScale = ContentScale.Crop
//     )
// }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    modifier: Modifier = Modifier,
    selectedCategory: Categories?,
    onCategorySelected: (Categories) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val borderColor =
        if (isFocused || selectedCategory != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = selectedCategory?.displayName ?: "",
                onValueChange = {},
                label = {
                    Text(
                        text = "Категория",
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ExposedDropdownMenu(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Categories.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddFormPreview() {
    AddAssetScreen(
        state = AddAssetScreenState(),
        onAction = { /* Do nothing */ }
    )
}