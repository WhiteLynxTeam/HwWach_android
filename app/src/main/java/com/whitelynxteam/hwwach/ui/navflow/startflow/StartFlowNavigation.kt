package com.whitelynxteam.hwwach.ui.navflow.startflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen.RegScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen.RegScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen.RegScreenViewModel

class StartFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.AuthScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {

            // ===== AUTH =====
            composable(Routes.AuthScreen.route) {

                val viewModel = hiltViewModel<AuthScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is AuthScreenEvent.NavigateToMain -> {
                                    finishFlow()
                                }

                                AuthScreenEvent.NavigateToReg -> {
                                    navController.navigate(Routes.RegScreen.route) {
                                        popUpTo(Routes.AuthScreen.route) {
                                            inclusive = true
                                        }
                                    }
                                }

                                is AuthScreenEvent.Exit -> {
                                    navController.popBackStack()
                                }
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
            // ===== REG =====
            composable(Routes.RegScreen.route) {

                val viewModel = hiltViewModel<RegScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is RegScreenEvent.NavigateToMain -> {
                                    finishFlow()
                                }

                                RegScreenEvent.NavigateToAuth -> {
                                    navController.navigate(Routes.AuthScreen.route) {
                                        popUpTo(Routes.RegScreen.route) {
                                            inclusive = true
                                        }
                                    }
                                }

                                is RegScreenEvent.Exit -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                RegScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }
        }
    }

    sealed class Routes(val route: String) {
        data object AuthScreen : Routes("StartFlowNavigator.AuthScreen")
        data object RegScreen : Routes("StartFlowNavigator.RegScreen")
    }
}