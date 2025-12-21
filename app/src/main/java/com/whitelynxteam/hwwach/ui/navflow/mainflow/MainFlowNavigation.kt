package com.whitelynxteam.hwwach.ui.navflow.mainflow

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.whitelynxteam.hwwach.ui.navigation.SubFlowNavigation

class MainFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : SubFlowNavigation(onFinished) {
    var currentRoute = Routes.MainScreen

    override val startRoute: String
        get() = Routes.MainScreen.route

    fun addFlow(builder: NavGraphBuilder) {
        with(builder) {
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
        /*        data object FavoritesScreen : Routes(
                    route = "MainFlowNavigator.FavoritesScreen",
                    iconActive = R.drawable.ic_favorites_active,
                    iconInactive = R.drawable.ic_favorites_inactive,
                    label = "Избранное",
                )
                data object InvitationsScreen : Routes(
                    route = "MainFlowNavigator.InvitationsScreen",
                    iconActive = R.drawable.ic_invitations_active,
                    iconInactive = R.drawable.ic_invitations_inactive,
                    label = "Приглашения",
                    )
                data object QuotasScreen : Routes(
                    route = "MainFlowNavigator.QuotasScreen",
                    iconActive = R.drawable.ic_quotas_active,
                    iconInactive = R.drawable.ic_quotas_inactive,
                    label = "Квоты",
                    )*/

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