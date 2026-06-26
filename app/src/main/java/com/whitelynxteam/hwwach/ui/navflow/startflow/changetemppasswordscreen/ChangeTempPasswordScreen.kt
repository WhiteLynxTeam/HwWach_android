package com.whitelynxteam.hwwach.ui.navflow.startflow.changetemppasswordscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitelynxteam.hwwach.ui.navflow.startflow.LoginTextField
import com.whitelynxteam.hwwach.ui.navflow.startflow.TextFieldType
import com.whitelynxteam.hwwach.ui.theme.Blue800
import com.whitelynxteam.hwwach.ui.theme.Gray250
import com.whitelynxteam.hwwach.ui.theme.Gray700
import com.whitelynxteam.hwwach.ui.theme.Gray800
import com.whitelynxteam.hwwach.ui.theme.Red500
import com.whitelynxteam.hwwach.ui.theme.White

@Composable
fun ChangeTempPasswordScreen(
    modifier: Modifier = Modifier,
    state: ChangeTempPasswordState,
    onAction: (ChangeTempPasswordAction) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray250)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                        text = "Смена временного пароля",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                    )
                    Text(
                        text = "Пожалуйста, установите новый пароль",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            
            LoginTextField(
                value = state.login,
                placeholderText = "Логин",
                onValueChange = { onAction(ChangeTempPasswordAction.InputLogin(it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LoginTextField(
                value = state.oldPassword,
                placeholderText = "Временный пароль",
                onValueChange = { onAction(ChangeTempPasswordAction.InputOldPassword(it)) },
                type = TextFieldType.PASSWORD
            )
            Spacer(modifier = Modifier.height(12.dp))

            LoginTextField(
                value = state.newPassword,
                placeholderText = "Новый пароль",
                onValueChange = { onAction(ChangeTempPasswordAction.InputNewPassword(it)) },
                type = TextFieldType.PASSWORD
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    onAction(ChangeTempPasswordAction.OnSubmitClicked)
                },
                shape = RoundedCornerShape(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray800,
                    disabledContainerColor = Gray800,
                    contentColor = White,
                    disabledContentColor = White,
                ),
                enabled = !state.isLoading,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(
                        text = "Сменить пароль",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            state.successMessage?.let { successMessage ->
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    text = successMessage,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center
                )
            }

            state.errorMessage?.let { errorMessage ->
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    text = errorMessage,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Red500,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { onAction(ChangeTempPasswordAction.OnBackClicked) }) {
                Text(
                    text = "Назад к авторизации",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Blue800,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeTempPasswordScreenPreview() {
    ChangeTempPasswordScreen(
        state = ChangeTempPasswordState(login = "test_user", oldPassword = "tempPassword"),
        onAction = {}
    )
}
