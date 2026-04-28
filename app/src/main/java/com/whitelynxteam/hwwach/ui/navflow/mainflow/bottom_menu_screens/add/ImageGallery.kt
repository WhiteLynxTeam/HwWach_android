package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.whitelynxteam.hwwach.R
import com.whitelynxteam.hwwach.common.extensions.PriorityAsyncImage
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum

@Composable
fun ImageGallery(
    modifier: Modifier = Modifier,
    photos: List<Photo>,
    onAction: (AddScreenAction) -> Unit,
    enabled: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos.size + 1) { index ->
            if (index < photos.size) {
                val photo = photos[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = enabled) { /* просмотр */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (!enabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                    }
                    PriorityAsyncImage(
                        photo = photo,
                        contentDescription = "Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (enabled) {
                        val isSent = photo.status == PhotoUploadStatusEnum.UPLOADED || photo.status == PhotoUploadStatusEnum.SYNCED
                        
                        if (isSent) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .background(Color.White.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onAction(AddScreenAction.RemovePhoto(photo)) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Статус загрузки фото (Нижний правый угол)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (photo.status) {
                            PhotoUploadStatusEnum.PENDING -> {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Pending",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            PhotoUploadStatusEnum.UPLOADING -> {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            PhotoUploadStatusEnum.UPLOADED,
                            PhotoUploadStatusEnum.SYNCED -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Synced",
                                    tint = Color(0xFF4CAF50), // Зеленый
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            PhotoUploadStatusEnum.FAILED -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Failed",
                                    tint = Color(0xFFF44336), // Красный
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable(enabled = enabled) { onAction(AddScreenAction.OpenImagePicker) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_photo),
                        contentDescription = "Add Photo"
                    )
                }
            }
        }
    }
}