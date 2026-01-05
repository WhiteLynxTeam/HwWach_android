package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.whitelynxteam.hwwach.R
import com.whitelynxteam.hwwach.ui.theme.Grey50

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomMenuScreen.Appliances to "Техника",
        BottomMenuScreen.Add to "Добавить",
        BottomMenuScreen.Profile to "Кабинет"
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.split('?')?.firstOrNull()

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Grey50
    ) {
        items.forEach { (screen, label) ->
            NavigationBarItem(
                icon = {
                    val isSelected = currentRoute == screen.route
                    val iconRes = if (isSelected) {
                        when (screen) {
                            BottomMenuScreen.Appliances -> R.drawable.ic_appliances_selected
                            BottomMenuScreen.Add -> R.drawable.ic_add_selected
                            BottomMenuScreen.Profile -> R.drawable.ic_profile_selected
                        }
                    } else {
                        when (screen) {
                            BottomMenuScreen.Appliances -> R.drawable.ic_appliances_unselected
                            BottomMenuScreen.Add -> R.drawable.ic_add_unselected
                            BottomMenuScreen.Profile -> R.drawable.ic_profile_unselected
                        }
                    }
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label
                    )
                },
                label = { Text(text = label) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class BottomMenuScreen(val route: String) {
    object Appliances : BottomMenuScreen("appliances")
    object Add : BottomMenuScreen("add")
    object Profile : BottomMenuScreen("profile")
}