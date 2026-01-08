package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitelynxteam.hwwach.R
import com.whitelynxteam.hwwach.ui.theme.Gray300
import com.whitelynxteam.hwwach.ui.theme.Gray500
import com.whitelynxteam.hwwach.ui.theme.Gray800
import com.whitelynxteam.hwwach.ui.theme.White

@Composable
fun AddForm(
    modifier: Modifier = Modifier,
    state: AddScreenState,
    onAction: (AddScreenAction) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        item {
            AddFormTextField(
                value = state.name,
                onValueChange = { onAction(AddScreenAction.InputName(it)) },
                label = "Название"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddFormTextField(
                value = state.category,
                onValueChange = { onAction(AddScreenAction.InputCategory(it)) },
                label = "Категория"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddFormTextField(
                value = state.inventoryNumber,
                onValueChange = { onAction(AddScreenAction.InputInventoryNumber(it)) },
                label = "Инвентаризационный номер"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddFormTextField(
                value = state.address,
                onValueChange = { onAction(AddScreenAction.InputAddress(it)) },
                label = "Адрес"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddFormTextField(
                value = state.comment,
                onValueChange = { onAction(AddScreenAction.InputComment(it)) },
                label = "Комментарий"
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(
                    listOf(
                        R.drawable.image,
                        R.drawable.image,
                        R.drawable.image,
                        R.drawable.image,
                        R.drawable.image,
                        R.drawable.image,
                        R.drawable.image
                    )
                ) { imageId ->
                    ImageCard(
                        modifier = Modifier
                            .widthIn(max = 68.dp)
                            .heightIn(max = 92.dp),
                        imageResId = imageId
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                onClick = {
                    onAction(AddScreenAction.OnSubmitClicked)
                },
                shape = RoundedCornerShape(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray800,
                    disabledContainerColor = Gray800,
                    contentColor = White,
                    disabledContentColor = White,
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

@Composable
fun AddFormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val borderColor = if (isFocused || value.isNotEmpty()) Gray800 else Gray300

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(36.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(36.dp)
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
                    color = Gray500
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Gray800,
                unfocusedTextColor = Gray800
            )
        )
    }
}

@Composable
private fun ImageCard(
    modifier: Modifier = Modifier,
    imageResId: Int
) {
    Image(
        modifier = modifier,
        painter = painterResource(id = imageResId),
        contentDescription = "Image",
        contentScale = ContentScale.Crop
    )
}

@Preview(showBackground = true)
@Composable
private fun AddFormPreview() {
    val viewModel = hiltViewModel<AddScreenViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    AddForm(
        state = uiState.value,
        onAction = {}
    )
}