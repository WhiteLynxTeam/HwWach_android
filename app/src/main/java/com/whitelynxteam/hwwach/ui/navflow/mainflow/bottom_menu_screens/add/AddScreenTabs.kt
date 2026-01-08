package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whitelynxteam.hwwach.ui.theme.Gray700
import com.whitelynxteam.hwwach.ui.theme.Gray800

@Composable
fun AddScreenTabs(
    selectedMode: AddScreenTab,
    onTabSelected: (AddScreenTab) -> Unit
) {
    val tabs = listOf(
        "Галерея" to AddScreenTab.Gallery,
        "Список" to AddScreenTab.List
    )

    val selectedTabIndex = if (selectedMode is AddScreenTab.Gallery) 0 else 1

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(2.dp)
                        .width(tabPositions[selectedTabIndex].width)
                        .background(Gray800)
                )
            }
        }
    ) {
        tabs.forEach { (label, mode) ->
            Tab(
                selected = selectedMode == mode,
                onClick = { onTabSelected(mode) },
                text = { Text(label) },
                unselectedContentColor = Gray700,
                selectedContentColor = Gray800,
            )
        }
    }
}