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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.whitelynxteam.hwwach.ui.FlowNavigation
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen.AuthScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen.RegScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen.RegScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen.RegScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.startflow.splashscreen.SplashScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.splashscreen.SplashScreenEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.splashscreen.SplashScreenViewModel
import com.whitelynxteam.hwwach.ui.navflow.startflow.forgotpasswordscreen.ForgotPasswordScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.forgotpasswordscreen.ForgotPasswordEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.forgotpasswordscreen.ForgotPasswordViewModel
import com.whitelynxteam.hwwach.ui.navflow.startflow.changetemppasswordscreen.ChangeTempPasswordScreen
import com.whitelynxteam.hwwach.ui.navflow.startflow.changetemppasswordscreen.ChangeTempPasswordEvent
import com.whitelynxteam.hwwach.ui.navflow.startflow.changetemppasswordscreen.ChangeTempPasswordViewModel

class StartFlowNavigation(
    val navController: NavHostController,
    onFinished: (routeName: String) -> Unit
) : FlowNavigation(onFinished) {
    override val startRoute: String
        get() = Routes.SplashScreen.route

    override fun addFlow(builder: NavGraphBuilder) {
        with(builder) {

            // ===== SPLASH =====
            composable(Routes.SplashScreen.route) {

                val viewModel = hiltViewModel<SplashScreenViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is SplashScreenEvent.NavigateToMain -> {
                                    finishFlow()
                                }

                                SplashScreenEvent.NavigateToAuth -> {
                                    navController.navigate(Routes.AuthScreen.route) {
                                        popUpTo(Routes.SplashScreen.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                SplashScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }

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
                                
                                AuthScreenEvent.NavigateToForgotPassword -> {
                                    navController.navigate(Routes.ForgotPasswordScreen.route)
                                }

                                is AuthScreenEvent.NavigateToChangeTempPassword -> {
                                    navController.navigate("StartFlowNavigator.ChangeTempPasswordScreen/${event.login}/${event.tempPassword}")
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
            
            // ===== FORGOT PASSWORD =====
            composable(Routes.ForgotPasswordScreen.route) {
                val viewModel = hiltViewModel<ForgotPasswordViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is ForgotPasswordEvent.Exit -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                ForgotPasswordScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }

            // ===== CHANGE TEMP PASSWORD =====
            composable(
                route = Routes.ChangeTempPasswordScreen.route,
                arguments = listOf(
                    navArgument("login") { type = NavType.StringType },
                    navArgument("tempPassword") { type = NavType.StringType }
                )
            ) {
                val viewModel = hiltViewModel<ChangeTempPasswordViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events, lifecycleOwner.lifecycle) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is ChangeTempPasswordEvent.Exit -> {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }

                ChangeTempPasswordScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onAction = viewModel::handleAction
                )
            }
        }
    }

    sealed class Routes(val route: String) {
        data object SplashScreen : Routes("StartFlowNavigator.SplashScreen")
        data object AuthScreen : Routes("StartFlowNavigator.AuthScreen")
        data object RegScreen : Routes("StartFlowNavigator.RegScreen")
        data object ForgotPasswordScreen : Routes("StartFlowNavigator.ForgotPasswordScreen")
        data object ChangeTempPasswordScreen : Routes("StartFlowNavigator.ChangeTempPasswordScreen/{login}/{tempPassword}")
    }
}