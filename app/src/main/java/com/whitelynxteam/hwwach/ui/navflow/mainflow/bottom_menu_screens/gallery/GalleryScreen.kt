package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.gallery

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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.whitelynxteam.hwwach.BuildConfig
import com.whitelynxteam.hwwach.ui.components.ImageGallery
import com.whitelynxteam.hwwach.ui.theme.Gray250
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    state: GalleryScreenState,
    onAction: (GalleryScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }
    var tempCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri -> onAction(GalleryScreenAction.AddImage(uri.toString())) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUriString?.let { onAction(GalleryScreenAction.AddImage(it)) }
        }
    }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val localOnAction = remember(onAction) {
        { action: GalleryScreenAction ->
            when (action) {
                GalleryScreenAction.ShowSourceSelector -> showSourceSheet = true
                GalleryScreenAction.OpenGallery -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                GalleryScreenAction.OpenCamera -> {
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

            ImageGallery(
                photos = state.photos,
                onImageClick = {id -> localOnAction(GalleryScreenAction.OpenFullImage(id))},
                onDeleteClick = {photo -> localOnAction(GalleryScreenAction.RemovePhoto(photo))},
                showSourceSelector = { localOnAction(GalleryScreenAction.ShowSourceSelector)},
                enabled = !state.isUploading && !state.isServerSyncing
            )

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
}

@Preview(showBackground = true)
@Composable
private fun AddScreenPreview() {
    GalleryScreen(
        state = GalleryScreenState(),
        onAction = {}
    )
}
