package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationDrawerItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.whitelynxteam.hwwach.BuildConfig
import com.whitelynxteam.hwwach.ui.theme.Gray250
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    state: AddScreenState,
    onAction: (AddScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }
    var tempCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri -> onAction(AddScreenAction.AddImage(uri.toString())) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUriString?.let { onAction(AddScreenAction.AddImage(it)) }
        }
    }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val localOnAction: (AddScreenAction) -> Unit = { action ->
        when (action) {
            AddScreenAction.OpenImagePicker -> showSourceSheet = true
            AddScreenAction.OpenCamera -> {
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
                onTabSelected = { mode -> onAction(AddScreenAction.SwitchMode(mode)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.currentMode is AddScreenTab.List) {
                AddForm(state = state, onAction = onAction)
            }

            if (state.currentMode is AddScreenTab.Gallery) {
                if (state.canSync || state.isSyncing || state.isInitializing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAction(AddScreenAction.SyncPendingPhotos) },
                            enabled = !state.isSyncing && !state.isInitializing,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        ) {
                            if (state.isSyncing || state.isInitializing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                            Text(
                                when {
                                    state.isInitializing -> "Синхронизация..."
                                    state.isSyncing -> "Отправка..."
                                    else -> "Отправить фото"
                                }
                            )
                        }

                        if (state.isSyncing) {
                            TextButton(
                                onClick = { onAction(AddScreenAction.CancelSync) },
                            ) {
                                Text("Отмена", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                if (state.syncError.isNotEmpty()) {
                    Text(
                        text = state.syncError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                ImageGallery(
                    photos = state.photos,
                    onAction = localOnAction,
                    enabled = !state.isSyncing && !state.isInitializing
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
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Сделать снимок") },
                    selected = false,
                    onClick = {
                        showSourceSheet = false
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
                    },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Выбрать из галереи") },
                    selected = false,
                    onClick = {
                        showSourceSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ImageOnly)
                        )
                    },
                    icon = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddScreenPreview() {
    AddScreen(
        state = AddScreenState(),
        onAction = {}
    )
}
