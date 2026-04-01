package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitelynxteam.hwwach.ui.theme.Gray250
import java.io.File

@Composable
fun AddScreen(
    modifier: Modifier = Modifier,
    state: AddScreenState,
    onAction: (AddScreenAction) -> Unit
) {

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AddScreenTabs(
                selectedMode = state.currentMode,
                onTabSelected = { mode ->
                    onAction(AddScreenAction.SwitchMode(mode))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.currentMode is AddScreenTab.List) {
                AddForm(state = state, onAction = onAction)
            }

            if (state.currentMode is AddScreenTab.Gallery) {
                ImageGallery(
                    images = state.images,
                    onAction = onAction
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.errorMessage.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<AddScreenViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Сохраняем Uri для камеры, чтобы знать, куда она сохранила фото после возврата
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var showSourceDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris ->
            uris.forEach { uri ->
                viewModel.handleAction(AddScreenAction.AddImage(uri.toString()))
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // Если фото успешно сделано, добавляем сохраненный Uri в стейт
                tempCameraUri?.let { uri ->
                    viewModel.handleAction(AddScreenAction.AddImage(uri.toString()))
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddScreenEvent.OpenImagePicker -> {
                    // 1. Теперь только показываем диалог выбора
                    showSourceDialog = true
                }
                is AddScreenEvent.OpenCamera -> {
                    val tempFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        tempFile
                    )
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                }
                AddScreenEvent.NavigateBack -> TODO()
                is AddScreenEvent.ShowErrorMessage -> TODO()
                AddScreenEvent.ShowSuccessMessage -> TODO()
            }
        }
    }

    if (showSourceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showSourceDialog = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }, // Полоска сверху шторки
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp) // Отступ снизу для красоты
            ) {
                Text(
                    text = "Добавить фото",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Сделать снимок") },
                    selected = false,
                    onClick = {
                        showSourceDialog = false
                        viewModel.handleAction(AddScreenAction.OpenCamera)
                    },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Выбрать из галереи") },
                    selected = false,
                    onClick = {
                        showSourceDialog = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    icon = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }

//    if (showSourceDialog) {
//        AlertDialog(
//            onDismissRequest = { showSourceDialog = false },
//            title = { Text("Добавить фото") },
//            text = { Text("Откуда вы хотите добавить фотографию?") },
//            confirmButton = {
//                TextButton(onClick = {
//                    showSourceDialog = false
//                    viewModel.handleAction(AddScreenAction.OpenCamera)
//                }) {
//                    Text("Камера")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = {
//                    showSourceDialog = false
//                    photoPickerLauncher.launch(
//                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                    )
//                }) {
//                    Text("Галерея")
//                }
//            }
//        )
//    }

    AddScreen(
        modifier = modifier,
        state = uiState.value,
        onAction = viewModel::handleAction
    )
}

@Preview(showBackground = true)
@Composable
private fun AddScreenPreview() {
    AddScreen(
        state = AddScreenState(),
        onAction = {}
    )
}