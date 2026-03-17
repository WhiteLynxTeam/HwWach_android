package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.whitelynxteam.hwwach.ui.theme.Gray250

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    menuItems: List<InnerMainFlowNavigation.Routes.BottomMenuDestination> = InnerMainFlowNavigation.Routes.menuRoutes,
    onTabSelected: (Int) -> Unit = {},
    selectedTabIndex: Int = 0
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Gray250
    ) {
        menuItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    val isSelected = index == selectedTabIndex
                    val iconRes = if (isSelected) item.iconActive else item.iconInactive
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                selected = index == selectedTabIndex,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                ),
                onClick = {
                    onTabSelected(index)
                }
            )
        }
    }
}