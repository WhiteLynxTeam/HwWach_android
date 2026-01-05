package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.AddScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.AppliancesScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.ProfileScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenState

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit
) {

    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomMenuScreen.Appliances.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomMenuScreen.Appliances.route) { AppliancesScreen() }
            composable(BottomMenuScreen.Add.route) { AddScreen() }
            composable(BottomMenuScreen.Profile.route) { ProfileScreen() }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(modifier = Modifier, state = MainScreenState(), onAction = {})
}