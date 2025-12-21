package com.whitelynxteam.hwwach.ui.navflow.startflow.authscreen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitelynxteam.hwwach.ui.navflow.startflow.LoginTextField
import com.whitelynxteam.hwwach.ui.theme.Grey700
import com.whitelynxteam.hwwach.ui.theme.Grey800
import com.whitelynxteam.hwwach.ui.theme.Red500
import com.whitelynxteam.hwwach.ui.theme.White

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    state: AuthScreenState,
    onAction: (AuthScreenAction) -> Unit
) {

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
                value = state.login,
                placeholderText = "Логин",
                onValueChange = { onAction(AuthScreenAction.InputLogin(it)) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            LoginTextField(
                value = state.password,
                placeholderText = "Пароль",
                onValueChange = { onAction(AuthScreenAction.InputPassword(it)) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    onAction(AuthScreenAction.OnAuthClicked)
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

            if (state.errorMessage != null) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.errorMessage
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

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(modifier = Modifier, state = AuthScreenState(), onAction = {})
}