package com.whitelynxteam.hwwach.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.whitelynxteam.hwwach.ui.navflow.startflow.AuthScreen
import javax.inject.Inject

class App @Inject constructor() {
    @Composable
    fun Render() {

        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "auth",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("auth") {
                    AuthScreen(
                        onAuthSuccess = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        },
                        onFinished = {}
                    )
                }
            }
        }

    }
}
