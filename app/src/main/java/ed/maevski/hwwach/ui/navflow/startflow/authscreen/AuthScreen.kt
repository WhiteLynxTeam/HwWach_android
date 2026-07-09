package ed.maevski.hwwach.ui.navflow.startflow.authscreen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.domain.models.RegStatusEnum
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.navflow.startflow.TextFieldType
import ed.maevski.hwwach.ui.theme.Blue800
import ed.maevski.hwwach.ui.theme.Gray250
import ed.maevski.hwwach.ui.theme.Gray400
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800
import ed.maevski.hwwach.ui.theme.Green700
import ed.maevski.hwwach.ui.theme.Red500
import ed.maevski.hwwach.ui.theme.White
import ed.maevski.hwwach.ui.theme.Yellow800

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    state: AuthScreenState,
    onAction: (AuthScreenAction) -> Unit
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
                        text = "Добро пожаловать",
                        fontSize = 22.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                    )
                    Text(
                        text = "Введите Ваш логин и пароль",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            LoginTextField(
                value = state.login,
                placeholderText = "Логин",
                type = TextFieldType.LOGIN,
                onValueChange = { onAction(AuthScreenAction.InputLogin(it.replace(" ", ""))) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            LoginTextField(
                value = state.password,
                placeholderText = "Пароль",
                type = TextFieldType.PASSWORD,
                onValueChange = { onAction(AuthScreenAction.InputPassword(it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                    containerColor = Gray800,
                    disabledContainerColor = if (state.isLoading) Gray800 else Gray400,
                    contentColor = White,
                    disabledContentColor = if (state.isLoading) White else Gray250,
                ),
                enabled = !state.isLoading && state.isPrivacyPolicyAccepted,
            )
            {
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
                        text = "Войти",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Privacy Police
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Checkbox(
                    checked = state.isPrivacyPolicyAccepted,
                    onCheckedChange = { onAction(AuthScreenAction.OnPrivacyPolicyChecked(it)) },
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
                                onAction(AuthScreenAction.OnPrivacyPolicyClicked)
                            }
                    }
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Кнопка Забыл пароль
            TextButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { onAction(AuthScreenAction.OnForgotPasswordClicked) }
            ) {
                Text(
                    text = "Забыл пароль?",
                    fontSize = 14.sp,
                    color = Gray700,
                )
            }

            // Отображение статуса регистрации
            state.registrationStatusMessage?.let { statusMessage ->
                val textColor = when (state.registrationStatus) {
                    RegStatusEnum.PENDING -> Yellow800
                    RegStatusEnum.APPROVED -> Green700
                    RegStatusEnum.REJECTED -> Red500
                    null -> Gray700
                }
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    text = statusMessage,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.errorMessage, // Передаем всю строку целиком с "\n"
                    textAlign = TextAlign.Center, // <--- Это магическая настройка
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Red500,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Ссылка на регистрацию
            TextButton(onClick = { onAction(AuthScreenAction.OnRegClicked) })
            {
                Text(
                    text = "Регистрация",
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
fun AuthScreenPreview() {
    AuthScreen(modifier = Modifier, state = AuthScreenState(), onAction = {})
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreviewWithError() {
    AuthScreen(
        modifier = Modifier,
        state = AuthScreenState(errorMessage = "Ошибка авторизации"),
        onAction = {})
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreviewWithErrorWithRegStatus() {
    AuthScreen(
        modifier = Modifier,
        state = AuthScreenState(
            errorMessage = "Ошибка авторизации",
            registrationStatusMessage = "Логин: Тест на проверке.",
            registrationStatus = RegStatusEnum.PENDING,
        ),
        onAction = {})
}