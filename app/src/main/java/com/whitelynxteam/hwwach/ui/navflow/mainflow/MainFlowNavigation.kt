package com.whitelynxteam.hwwach.ui.navflow.mainflow

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
import androidx.navigation.compose.rememberNavController
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.InnerMainFlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreenViewModel

class MainFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.MainScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {
            composable(Routes.MainScreen.route) {
                val innerMainNavController = rememberNavController()
                val innerMainFlowNavigation =
                    InnerMainFlowNavigation(innerMainNavController) { }

                val viewModel = hiltViewModel<MainScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is MainScreenEvent.NavigateToBottomMenuItem -> {
                                    innerMainFlowNavigation.navigateToIndex(event.index)
                                }
                            }
                        }
                    }
                }

                MainScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction,
                    innerMainFlowNavigation = innerMainFlowNavigation
                )
            }
        }
    }

    sealed class Routes(val route: String) {
        data object MainScreen : Routes(route = "MainFlowNavigator.MainScreen")
    }
}