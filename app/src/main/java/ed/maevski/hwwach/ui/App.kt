package ed.maevski.hwwach.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.irepositories.TokenState
import ed.maevski.hwwach.ui.navflow.mainflow.MainFlowNavigation
import ed.maevski.hwwach.ui.navflow.startflow.StartFlowNavigation
import javax.inject.Inject

class App @Inject constructor(
    private val tokensRepository: ITokensRepository
) {
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
                    navController.currentDestination?.route?.let { currentRoute ->
                        popUpTo(currentRoute) {
                            inclusive = true
                        }
                    }
                }
            }

        LaunchedEffect(Unit) {
            tokensRepository.tokenState.collect { state ->
                if (state is TokenState.Unauthenticated) {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute != null && currentRoute.startsWith("MainFlowNavigator")) {
                        navController.navigate(startFlowNavigation.startRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
