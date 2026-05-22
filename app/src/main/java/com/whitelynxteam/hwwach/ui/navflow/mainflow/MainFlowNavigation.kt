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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.addasset.AddAssetScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.addasset.AddAssetScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.mainflow.addasset.AddAssetScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.mainflow.fullimage.FullImageScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.fullimage.FullImageScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.mainflow.fullimage.FullImageScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.InnerMainFlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen.MainScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.mainflow.assetdetail.AssetDetailScreen
import com.whitelynxteam.hwwach.ui.navflow.mainflow.assetdetail.AssetDetailScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.mainflow.assetdetail.AssetDetailScreenViewModel


class MainFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.MainScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {

            // ===== MAIN =====
            composable(Routes.MainScreen.route) {
                val innerMainNavController = rememberNavController()
                val innerMainFlowNavigation = InnerMainFlowNavigation(
                    navController = innerMainNavController,
                    onNavigateToFullImage = { clientId ->
                        navController.navigate("MainFlowNavigator.FullImageScreen/$clientId")
                    },
                    onNavigateToAddAsset = {
                        navController.navigate("MainFlowNavigator.AddAssetScreen")
                    },
                    onNavigateToAssetDetail = { clientId ->
                        navController.navigate("MainFlowNavigator.AssetDetailScreen/$clientId")
                    },
                    onLogout = {
                        navController.navigate("StartFlowNavigator.AuthScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onFinished = {}
                )

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

            // ===== Добавление нового актива в инвентаризацию =====
            composable(Routes.AddAssetScreen.route) {
                val viewModel = hiltViewModel<AddAssetScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                AddAssetScreenEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                                is AddAssetScreenEvent.NavigateToFullImage -> {
                                    navController.navigate("MainFlowNavigator.FullImageScreen/${event.clientId}")
                                }
                                AddAssetScreenEvent.ShowSuccessMessage -> {
                                    val context = navController.context
                                    android.widget.Toast.makeText(context, "Актив успешно добавлен", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                else -> {}
                            }
                        }
                    }
                }

                AddAssetScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction,
                )
            }

            // ===== Полноэкранный показ картинки =====
            composable(
                route = Routes.FullImageScreen.route,
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val viewModel = hiltViewModel<FullImageScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is FullImageScreenEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                FullImageScreen(
                    state = state,
                    onAction = viewModel::handleAction,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ===== Детализация актива (Инвентарная карточка) =====
            composable(
                route = Routes.AssetDetailScreen.route,
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val viewModel = hiltViewModel<AssetDetailScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                AssetDetailScreenEvent.NavigateBack -> {
                                    navController.popBackStack()
                                }
                                AssetDetailScreenEvent.NavigateToEdit -> {
                                    val context = navController.context
                                    android.widget.Toast.makeText(context, "Редактирование (заглушка)", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                is AssetDetailScreenEvent.NavigateToFullImage -> {
                                    navController.navigate("MainFlowNavigator.FullImageScreen/${event.clientId}")
                                }
                                AssetDetailScreenEvent.ShowDeleteConfirmation -> {
                                    val context = navController.context
                                    android.widget.Toast.makeText(context, "Удаление (заглушка)", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                is AssetDetailScreenEvent.ShowErrorMessage -> {
                                    val context = navController.context
                                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                AssetDetailScreen(
                    state = state,
                    onAction = viewModel::handleAction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    sealed class Routes(val route: String) {
        data object MainScreen : Routes(route = "MainFlowNavigator.MainScreen")
        data object AddAssetScreen : Routes(route = "MainFlowNavigator.AddAssetScreen")
        data object FullImageScreen : Routes(route = "MainFlowNavigator.FullImageScreen/{clientId}")
        data object AssetDetailScreen : Routes(route = "MainFlowNavigator.AssetDetailScreen/{clientId}")
    }
}