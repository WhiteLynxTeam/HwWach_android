package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.whitelynxteam.hwwach.ui.components.BottomNavigationBar
import com.whitelynxteam.hwwach.ui.theme.Gray250

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit,
    innerMainFlowNavigation: InnerMainFlowNavigation
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Gray250,
        bottomBar = {
            BottomNavigationBar(
                menuItems = state.bottomMenuItems,
                onTabSelected = { tabIndex ->
                    onAction(MainScreenAction.OnBottomMenuItemClick(tabIndex))
                },
                selectedTabIndex = state.selectedTabIndex
            )
        }
    ) { padding ->
        NavHost(
            navController = innerMainFlowNavigation.navController,
            startDestination = innerMainFlowNavigation.startRoute,
            modifier = Modifier.padding(padding),
        ) {
            innerMainFlowNavigation.addFlow(this)
        }
    }
}

/*
@Preview
@Composable
fun MainScreenPreview() {
    val innerMainNavController = rememberNavController()
    val innerMainFlowNavigation =
        InnerMainFlowNavigation(innerMainNavController) { }
    MainScreen(
        modifier = Modifier,
        state = MainScreenState(),
        onAction = {},
        innerMainFlowNavigation = innerMainFlowNavigation
    )
}*/
