package ed.maevski.hwwach.ui.navflow.startflow.regscreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import ed.maevski.hwwach.ui.navflow.startflow.FormError
import ed.maevski.hwwach.ui.navflow.startflow.FormFieldSpacer
import ed.maevski.hwwach.ui.navflow.startflow.FormPrimaryButton
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.navflow.startflow.StartFormLayout
import ed.maevski.hwwach.ui.navflow.startflow.TextFieldType
import ed.maevski.hwwach.ui.theme.Blue800
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800

@Composable
fun RegScreen(
    modifier: Modifier = Modifier,
    state: RegScreenState,
    onAction: (RegScreenAction) -> Unit
) {
    StartFormLayout(
        modifier = modifier,
        title = "Регистрация",
        subtitle = "Введите Ваши данные",
        onBack = { onAction(RegScreenAction.OnBackClicked) }
    ) {
        RegOrganizationBlock(state = state, onAction = onAction)

        Spacer(modifier = Modifier.height(24.dp))

        LoginTextField(
            value = state.lastName,
            placeholderText = "Фамилия",
            onValueChange = { onAction(RegScreenAction.InputLastName(it)) }
        )

        FormFieldSpacer()

        LoginTextField(
            value = state.firstName,
            placeholderText = "Имя",
            onValueChange = { onAction(RegScreenAction.InputFirstName(it)) }
        )

        FormFieldSpacer()

        LoginTextField(
            value = state.middleName,
            placeholderText = "Отчество (необязательно)",
            onValueChange = { onAction(RegScreenAction.InputMiddleName(it)) }
        )

        FormFieldSpacer()

        LoginTextField(
            value = state.phone,
            placeholderText = "Телефон (необязательно)",
            onValueChange = { onAction(RegScreenAction.InputPhone(it)) },
            //[flag.yellow  - посмотреть поле key, разобраться с типами]
//            keyboardType = KeyboardType.Phone
        )

        FormFieldSpacer()

        LoginTextField(
            value = state.position,
            placeholderText = "Должность (необязательно)",
            onValueChange = { onAction(RegScreenAction.InputPosition(it)) },
        )

        FormFieldSpacer()

        LoginTextField(
            value = state.login,
            placeholderText = "Логин",
            type = TextFieldType.LOGIN,
            onValueChange = { onAction(RegScreenAction.InputLogin(it.replace(" ", ""))) },
        )

        FormFieldSpacer()

        LoginTextField(
            value = state.password,
            placeholderText = "Пароль",
            type = TextFieldType.PASSWORD,
            onValueChange = { onAction(RegScreenAction.InputPassword(it)) }
        )

        FormFieldSpacer()

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

        FormPrimaryButton(
            text = "Регистрация",
            isLoading = state.isLoading,
            enabled = state.isPrivacyPolicyAccepted,
            onClick = { onAction(RegScreenAction.OnRegClicked) }
        )

        FormError(state.errorMessage)

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
