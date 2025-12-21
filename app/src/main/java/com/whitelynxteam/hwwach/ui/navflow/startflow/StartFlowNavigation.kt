package com.whitelynxteam.hwwach.ui.navflow.startflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenViewModel
import kotlinx.coroutines.flow.collectLatest

class StartFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) {
    val startRoute: String
        get() = Routes.AuthScreen.route

    fun addFlow(builder: NavGraphBuilder) {
        with(builder) {

            // ===== AUTH =====
            composable(Routes.AuthScreen.route) {

                val viewModel = hiltViewModel<AuthScreenViewModel>()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            is AuthScreenEvent.NavigateToMain -> navController.navigate(Routes.AuthScreen.route)

                            is AuthScreenEvent.Exit -> {
                                navController.popBackStack()
                            }
                        }
                    }
                }

                AuthScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }
        }
    }

    sealed class Routes(val route: String) {
        data object AuthScreen : Routes("StartFlowNavigator.AuthScreen")
    }
}