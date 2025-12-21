package com.whitelynxteam.hwwach.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.whitelynxteam.hwwach.ui.navflow.mainflow.MainFlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.startflow.StartFlowNavigation
import javax.inject.Inject

class App @Inject constructor() {
    @Composable
    fun Render() {
        val navController = rememberNavController()

        val mainFlowNavigation =
            MainFlowNavigation(navController) {
                // при завершении данной ветки навигации приложение должно закрыться
            }

        val startFlowNavigation =
            StartFlowNavigation(navController) {
                navController.navigate(mainFlowNavigation.startRoute) {
                    popUpTo(StartFlowNavigation.Routes.AuthScreen.route) {
                        inclusive = true
                    }
                }
            }

        val flows: NavGraphBuilder.() -> Unit = {
            startFlowNavigation.addFlow(this)
            mainFlowNavigation.addFlow(this)
        }

        NavHost(
            navController = navController,
            startDestination = startFlowNavigation.startRoute,
            builder = flows
        )

    }
}
