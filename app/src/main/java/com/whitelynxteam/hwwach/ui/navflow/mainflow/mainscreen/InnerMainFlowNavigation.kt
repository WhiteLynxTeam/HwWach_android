package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.whitelynxteam.hwwach.R
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add.AddScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.appliances.AppliancesScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.loading.LoadingScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile.ProfileScreen

class InnerMainFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.LoadingScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {
            composable(Routes.LoadingScreen.route) {
                LoadingScreen(
                    modifier = Modifier.fillMaxSize(),
                )
            }
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
        if (index >= 0 && index < Routes.menuRoutes.size) {
            val route = Routes.menuRoutes[index].route
            navigateToRoute(route)
        }
    }

    sealed class Routes(val route: String) {

        // 1. Создаем интерфейс только для тех, кто должен быть в меню
        interface BottomMenuDestination {
            val route: String
            val iconActive: Int
            val iconInactive: Int
            val label: String
        }

        // 2. LoadingScreen — это Route, но НЕ BottomMenuDestination
        data object LoadingScreen : Routes(
            route = "InnerMainFlowNavigation.LoadingScreen"
        )

        // 3. Остальные — и Route, и BottomMenuDestination
        data object AddScreen : Routes(
            route = "InnerMainFlowNavigation.AddScreen"
        ), BottomMenuDestination {
            override val iconActive = R.drawable.ic_add_selected
            override val iconInactive = R.drawable.ic_add_unselected
            override val label = "Добавить"
        }

        data object AppliancesScreen : Routes(
            route = "InnerMainFlowNavigation.AppliancesScreen"
        ), BottomMenuDestination {
            override val iconActive = R.drawable.ic_appliances_selected
            override val iconInactive = R.drawable.ic_appliances_unselected
            override val label = "Техника"
        }

        data object ProfileScreen : Routes(
            route = "InnerMainFlowNavigation.ProfileScreen"
        ), BottomMenuDestination {
            override val iconActive = R.drawable.ic_profile_selected
            override val iconInactive = R.drawable.ic_profile_unselected
            override val label = "Кабинет"
        }

        companion object {
            //Использовать val с ленивой инициализацией (by lazy),
            // чтобы доступ к объектам в списке происходил после полной инициализации всех объектов.

            //без lazy первый элемент был null

            //Для надежного кода избегай прямого доступа к объектам sealed class
            // в статической инициализации companion без ленивой обёртки

            // Теперь список содержит только элементы меню.
            // Тип списка — List<BottomMenuDestination>, и у них иконки НЕ nullable.
            val menuRoutes: List<BottomMenuDestination> by lazy {
                listOf(
                    AppliancesScreen,
                    AddScreen,
                    ProfileScreen,
                )
            }
        }
    }
}