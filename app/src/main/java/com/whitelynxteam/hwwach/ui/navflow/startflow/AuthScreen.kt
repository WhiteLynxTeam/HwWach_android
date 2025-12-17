package com.whitelynxteam.hwwach.ui.navflow.startflow

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.whitelynxteam.hwwach.ui.theme.Grey700
import com.whitelynxteam.hwwach.ui.theme.Grey800
import com.whitelynxteam.hwwach.ui.theme.Red500
import com.whitelynxteam.hwwach.ui.theme.White

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthScreenViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit,
    onFinished: () -> Unit
) {

    val state = viewModel.state.collectAsState()
    when (val currentState = state.value) {
        is AuthState.Input -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column() {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = 4.dp),
                                text = "Добро пожаловать",
                                fontSize = 22.sp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Grey800
                            )
                            Text(
                                text = "Введите Ваш логин и пароль",
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Grey700
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                    LoginTextField(
                        value = currentState.login,
                        placeholderText = "Логин",
                        onValueChange = { viewModel.processCommand(AuthCommand.InputLogin(it)) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    LoginTextField(
                        value = currentState.password,
                        placeholderText = "Пароль",
                        onValueChange = { viewModel.processCommand(AuthCommand.InputPassword(it)) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = {
                            viewModel.processCommand(AuthCommand.Login)
                        },
                        shape = RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Grey800,
                            disabledContainerColor = Grey800,
                            contentColor = White,
                            disabledContentColor = White,
                        )
                    ) {
                        Text(
                            text = "Войти",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (state.value is AuthState.Input) {
                        val inputState = state.value as AuthState.Input

                        if (inputState.errorAuth != null) {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                inputState.errorAuth
                                    .split("\n")
                                    .forEach {
                                        Text(
                                            modifier = Modifier.padding(bottom = 4.dp),
                                            text = it,
                                            fontSize = 14.sp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Red500
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }

        AuthState.Finished -> {
            LaunchedEffect(key1 = Unit) {
                onFinished()
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(modifier = Modifier, onAuthSuccess = {}, onFinished = {})
}