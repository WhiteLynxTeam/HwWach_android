package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.whitelynxteam.hwwach.R
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.AddScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.AppliancesScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.ProfileScreen

class InnerMainFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.AppliancesScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {
            composable(Routes.AddScreen.route) {
                AddScreen(
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.AppliancesScreen.route) {
                AppliancesScreen(
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.ProfileScreen.route) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    fun navigateToRoute(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToIndex(index: Int) {
        if (index >= 0 && index < Routes.allRoutes.size) {
            val route = Routes.allRoutes[index].route
            navigateToRoute(route)
        }
    }

    sealed class Routes(
        val route: String,
        val iconActive: Int,
        val iconInactive: Int,
        val label: String,
    ) {
        data object AddScreen : Routes(
            route = "InnerMainFlowNavigation.AddScreen",
            iconActive = R.drawable.ic_add_selected,
            iconInactive = R.drawable.ic_add_unselected,
            label = "Добавить",
        )
        data object AppliancesScreen : Routes(
            route = "InnerMainFlowNavigation.AppliancesScreen",
            iconActive = R.drawable.ic_appliances_selected,
            iconInactive = R.drawable.ic_appliances_unselected,
            label = "Техника",
        )
        data object ProfileScreen : Routes(
            route = "InnerMainFlowNavigation.ProfileScreen",
            iconActive = R.drawable.ic_profile_selected,
            iconInactive = R.drawable.ic_profile_unselected,
            label = "Кабинет",
        )

        companion object {
            //Использовать val с ленивой инициализацией (by lazy),
            // чтобы доступ к объектам в списке происходил после полной инициализации всех объектов.

            //без lazy первый элемент был null

            //Для надежного кода избегай прямого доступа к объектам sealed class
            // в статической инициализации companion без ленивой обёртки

            val allRoutes: List<Routes> by lazy {
                listOf(
                    AppliancesScreen,
                    AddScreen,
                    ProfileScreen,
                )
            }
        }
    }
}