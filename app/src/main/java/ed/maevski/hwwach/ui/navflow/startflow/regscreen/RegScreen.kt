package ed.maevski.hwwach.ui.navflow.startflow.regscreen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.navflow.startflow.TextFieldType
import ed.maevski.hwwach.ui.theme.Blue800
import ed.maevski.hwwach.ui.theme.Gray250
import ed.maevski.hwwach.ui.theme.Gray400
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800
import ed.maevski.hwwach.ui.theme.White

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = { onAction(RegScreenAction.OnBackClicked) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Gray800
                    )
                }
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
                placeholderText = "Отчество (необязательно)",
                onValueChange = { onAction(RegScreenAction.InputMiddleName(it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.phone,
                placeholderText = "Телефон (необязательно)",
                onValueChange = { onAction(RegScreenAction.InputPhone(it)) },
                //[flag.yellow  - посмотреть поле key, разобраться с типами]
//                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.position,
                placeholderText = "Должность (необязательно)",
                onValueChange = { onAction(RegScreenAction.InputPosition(it)) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.login,
                placeholderText = "Логин",
                type = TextFieldType.LOGIN,
                onValueChange = { onAction(RegScreenAction.InputLogin(it.replace(" ", ""))) },
            )
            Spacer(modifier = Modifier.height(20.dp))

            LoginTextField(
                value = state.password,
                placeholderText = "Пароль",
                type = TextFieldType.PASSWORD,
                onValueChange = { onAction(RegScreenAction.InputPassword(it)) }
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Policy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Checkbox(
                    checked = state.isPrivacyPolicyAccepted,
                    onCheckedChange = { onAction(RegScreenAction.OnPrivacyPolicyChecked(it)) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Gray800,
                        uncheckedColor = Gray700
                    )
                )
                val annotatedString = buildAnnotatedString {
                    append("Я согласен с ")
                    pushStringAnnotation(tag = "policy", annotation = "policy")
                    withStyle(style = SpanStyle(color = Blue800, textDecoration = TextDecoration.Underline)) {
                        append("Политикой конфиденциальности")
                    }
                    pop()
                }
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray700,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onAction(RegScreenAction.OnPrivacyPolicyClicked)
                            }
                    }
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))

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
                    disabledContainerColor = if (state.isLoading) Gray800 else Gray400,
                    contentColor = White,
                    disabledContentColor = if (state.isLoading) White else Gray250,
                ),
                enabled = !state.isLoading && state.isPrivacyPolicyAccepted,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(
                        text = "Регистрация",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
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

@Preview(showBackground = true)
@Composable
fun RegScreenWithErrorPreview() {
    RegScreen(modifier = Modifier, state = RegScreenState(errorMessage = "Пример ошибки регистрации"), onAction = {})
}