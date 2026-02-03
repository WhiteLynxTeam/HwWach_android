package com.whitelynxteam.hwwach.ui.navflow.startflow.regscreen

import androidx.compose.foundation.background
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitelynxteam.hwwach.ui.navflow.startflow.LoginTextField
import com.whitelynxteam.hwwach.ui.navflow.startflow.TextFieldType
import com.whitelynxteam.hwwach.ui.theme.Gray250
import com.whitelynxteam.hwwach.ui.theme.Gray700
import com.whitelynxteam.hwwach.ui.theme.Gray800
import com.whitelynxteam.hwwach.ui.theme.White

@Composable
fun RegScreen(
    modifier: Modifier = Modifier,
    state: RegScreenState,
    onAction: (RegScreenAction) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray250)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
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
                        text = "Регистрация",
                        fontSize = 22.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                    )
                    Text(
                        text = "Введите Ваши данные",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            }


            Spacer(modifier = Modifier.height(40.dp))

            LoginTextField(
                value = state.lastName,
                placeholderText = "Фамилия",
                onValueChange = { onAction(RegScreenAction.InputLastName(it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.firstName,
                placeholderText = "Имя",
                onValueChange = { onAction(RegScreenAction.InputFirstName(it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.middleName,
                placeholderText = "Отчество",
                onValueChange = { onAction(RegScreenAction.InputMiddleName(it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.phone,
                placeholderText = "Телефон",
                onValueChange = { onAction(RegScreenAction.InputPhone(it)) },
                //[flag.yellow  - посмотреть поле key, разобраться с типами]
//                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.position,
                placeholderText = "Должность",
                onValueChange = { onAction(RegScreenAction.InputPosition(it)) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.login,
                placeholderText = "Логин",
                onValueChange = { onAction(RegScreenAction.InputLogin(it)) },
            )
            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.password,
                placeholderText = "Пароль",
                type = TextFieldType.PASSWORD,
                onValueChange = { onAction(RegScreenAction.InputPassword(it)) }
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    onAction(RegScreenAction.OnRegClicked)
                },
                shape = RoundedCornerShape(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray800,
                    disabledContainerColor = Gray800,
                    contentColor = White,
                    disabledContentColor = White,
                )
            ) {
                Text(
                    text = "Регистрация",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ссылка на авторизацию
            TextButton(onClick = { onAction(RegScreenAction.OnAuthClicked) })
            {
                Text(
                    text = "Авторизация",
                    fontSize = 14.sp,
                    color = Color(0xFF3D5AFE)
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun RegScreenPreview() {
    RegScreen(modifier = Modifier, state = RegScreenState(), onAction = {})
}