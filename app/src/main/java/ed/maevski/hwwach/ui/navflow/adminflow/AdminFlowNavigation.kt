package ed.maevski.hwwach.ui.navflow.adminflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ed.maevski.hwwach.ui.FlowNavigation
import ed.maevski.hwwach.ui.navflow.adminflow.hub.AdminHubAction
import ed.maevski.hwwach.ui.navflow.adminflow.hub.AdminHubEvent
import ed.maevski.hwwach.ui.navflow.adminflow.hub.AdminHubScreen
import ed.maevski.hwwach.ui.navflow.adminflow.hub.AdminHubViewModel
import ed.maevski.hwwach.ui.navflow.adminflow.passwordresets.PasswordResetsAction
import ed.maevski.hwwach.ui.navflow.adminflow.passwordresets.PasswordResetsEvent
import ed.maevski.hwwach.ui.navflow.adminflow.passwordresets.PasswordResetsScreen
import ed.maevski.hwwach.ui.navflow.adminflow.passwordresets.PasswordResetsViewModel
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequestdetail.RegistrationRequestDetailEvent
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequestdetail.RegistrationRequestDetailScreen
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequestdetail.RegistrationRequestDetailViewModel
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests.RegistrationRequestsAction
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests.RegistrationRequestsEvent
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests.RegistrationRequestsScreen
import ed.maevski.hwwach.ui.navflow.adminflow.registrationrequests.RegistrationRequestsViewModel

/**
 * Админский flow. Вынесен отдельно от MainFlow, чтобы со временем его можно было
 * отделить в отдельную сборку или модуль без переписывания навигации пользователя.
 * Вход — из «Кабинета», только для роли ADMIN (сервер дополнительно проверяет роль).
 */
class AdminFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.AdminHubScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {

            // ===== Хаб админских функций =====
            composable(Routes.AdminHubScreen.route) {
                val viewModel = hiltViewModel<AdminHubViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(Unit) {
                    viewModel.handleAction(AdminHubAction.OnScreenShown)
                }

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                AdminHubEvent.NavigateToRegistrationRequests -> {
                                    navController.navigate(Routes.RegistrationRequestsScreen.route)
                                }
                                AdminHubEvent.NavigateToPasswordResets -> {
                                    navController.navigate(Routes.PasswordResetsScreen.route)
                                }
                                AdminHubEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                AdminHubScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }

            // ===== Список заявок на регистрацию =====
            composable(Routes.RegistrationRequestsScreen.route) {
                val viewModel = hiltViewModel<RegistrationRequestsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(Unit) {
                    viewModel.handleAction(RegistrationRequestsAction.OnScreenShown)
                }

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is RegistrationRequestsEvent.NavigateToDetail -> {
                                    navController.navigate(Routes.RegistrationRequestDetailScreen.create(event.id))
                                }
                                RegistrationRequestsEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                RegistrationRequestsScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }

            // ===== Заявки на сброс пароля =====
            composable(Routes.PasswordResetsScreen.route) {
                val viewModel = hiltViewModel<PasswordResetsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.handleAction(PasswordResetsAction.OnScreenShown)
                }

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is PasswordResetsEvent.ShowMessage -> {
                                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                PasswordResetsEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                PasswordResetsScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }

            // ===== Детали заявки: одобрение / отклонение =====
            composable(
                route = Routes.RegistrationRequestDetailScreen.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                val viewModel = hiltViewModel<RegistrationRequestDetailViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current
                val context = LocalContext.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is RegistrationRequestDetailEvent.ShowMessage -> {
                                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                RegistrationRequestDetailEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                RegistrationRequestDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }
        }
    }

    sealed class Routes(val route: String) {
        data object AdminHubScreen : Routes(route = "$ADMIN_FLOW_ROUTE_PREFIX.AdminHubScreen")
        data object RegistrationRequestsScreen : Routes(route = "$ADMIN_FLOW_ROUTE_PREFIX.RegistrationRequestsScreen")
        data object PasswordResetsScreen : Routes(route = "$ADMIN_FLOW_ROUTE_PREFIX.PasswordResetsScreen")
        data object RegistrationRequestDetailScreen : Routes(route = "$ADMIN_FLOW_ROUTE_PREFIX.RegistrationRequestDetailScreen/{id}") {
            fun create(id: String) = "$ADMIN_FLOW_ROUTE_PREFIX.RegistrationRequestDetailScreen/$id"
        }
    }
}

/** Префикс маршрутов админского flow — по нему App.kt понимает, что экран требует авторизации */
const val ADMIN_FLOW_ROUTE_PREFIX = "AdminFlowNavigator"
