package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitelynxteam.hwwach.ui.theme.Gray250

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

@Composable
fun AddScreen(
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<AddScreenViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

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