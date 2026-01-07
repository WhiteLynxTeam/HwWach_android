package com.whitelynxteam.hwwach.ui.navflow.mainflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.InnerMainFlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreenViewModel
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.startflow.StartFlowNavigation.Routes
import kotlinx.coroutines.flow.collectLatest

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
                val state by viewModel.state.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            is MainScreenEvent.NavigateToBottomMenuItem -> {
                                innerMainFlowNavigation.navigateToIndex(event.index)
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

    sealed class Routes(
        val route: String,
//        val iconActive: Int,
//        val iconInactive: Int,
        val label: String,
    ) {
        data object MainScreen : Routes(
            route = "MainFlowNavigator.MainScreen",
//            iconActive = R.drawable.ic_showcase_active,
//            iconInactive = R.drawable.ic_showcase_inactive,
            label = "Главный",
        )

        companion object {
            //Использовать val с ленивой инициализацией (by lazy),
            // чтобы доступ к объектам в списке происходил после полной инициализации всех объектов.

            //без lazy первый элемент был null

            //Для надежного кода избегай прямого доступа к объектам sealed class
            // в статической инициализации companion без ленивой обёртки

            val allRoutes: List<Routes> by lazy {
                listOf(
                    MainScreen,
                    /*                    FavoritesScreen,
                                        InvitationsScreen,
                                        QuotasScreen,*/
                )
            }
        }
    }
}