package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {


    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBarSample()
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}