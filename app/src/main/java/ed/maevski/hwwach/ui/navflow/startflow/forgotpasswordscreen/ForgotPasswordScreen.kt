package ed.maevski.hwwach.ui.navflow.startflow.forgotpasswordscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import ed.maevski.hwwach.domain.models.OrgNumber
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.navflow.startflow.TextFieldType
import ed.maevski.hwwach.ui.theme.Blue800
import ed.maevski.hwwach.ui.theme.Gray250
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800
import ed.maevski.hwwach.ui.theme.Red500
import ed.maevski.hwwach.ui.theme.White

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    state: ForgotPasswordState,
    onAction: (ForgotPasswordAction) -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray250)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = "Восстановление пароля",
                fontSize = 22.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800
            )
            Text(
                textAlign = TextAlign.Center,
                text = when (state.step) {
                    ForgotPasswordStep.INPUT -> "Почта — придёт код, пароль смените сами.\nЛогин — заявка уйдёт администратору организации"
                    ForgotPasswordStep.CODE -> "Код из письма на ${state.email} и новый пароль"
                    ForgotPasswordStep.DONE -> ""
                },
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
            Spacer(modifier = Modifier.height(32.dp))

            when (state.step) {
                ForgotPasswordStep.INPUT -> InputStep(state, onAction)
                ForgotPasswordStep.CODE -> CodeStep(state, onAction)
                ForgotPasswordStep.DONE -> Unit
            }

            state.successMessage?.let { successMessage ->
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    text = successMessage,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50), // Зеленый цвет для успеха
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

            if (state.step == ForgotPasswordStep.DONE) {
                Spacer(modifier = Modifier.height(16.dp))
                MainButton(text = "Войти", isLoading = false) { onAction(ForgotPasswordAction.OnBackClicked) }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                // Ссылка назад
                TextButton(onClick = { onAction(ForgotPasswordAction.OnBackClicked) })
                {
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
}

@Composable
private fun InputStep(state: ForgotPasswordState, onAction: (ForgotPasswordAction) -> Unit) {
    LoginTextField(
        value = state.login,
        placeholderText = "Почта или логин",
        type = TextFieldType.LOGIN,
        onValueChange = { onAction(ForgotPasswordAction.InputLogin(it.replace(" ", ""))) }
    )
    // Номер организации нужен только для логина: почта уникальна во всей системе
    if (!state.isEmail) {
        Spacer(modifier = Modifier.height(12.dp))
        OrgNumberBlock(state = state, onAction = onAction)
    }
    Spacer(modifier = Modifier.height(20.dp))
    MainButton(
        text = if (state.isEmail) "Получить код" else "Отправить заявку администратору",
        isLoading = state.isLoading,
    ) { onAction(ForgotPasswordAction.OnSubmitClicked) }
}

@Composable
private fun CodeStep(state: ForgotPasswordState, onAction: (ForgotPasswordAction) -> Unit) {
    LoginTextField(
        value = state.code,
        placeholderText = "Код из письма",
        type = TextFieldType.NUMBER,
        onValueChange = { onAction(ForgotPasswordAction.InputCode(it)) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    LoginTextField(
        value = state.newPassword,
        placeholderText = "Новый пароль",
        type = TextFieldType.PASSWORD,
        onValueChange = { onAction(ForgotPasswordAction.InputNewPassword(it)) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    LoginTextField(
        value = state.repeatPassword,
        placeholderText = "Повторите пароль",
        type = TextFieldType.PASSWORD,
        onValueChange = { onAction(ForgotPasswordAction.InputRepeatPassword(it)) }
    )
    Spacer(modifier = Modifier.height(20.dp))
    MainButton(text = "Сменить пароль", isLoading = state.isLoading) { onAction(ForgotPasswordAction.OnConfirmClicked) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = { onAction(ForgotPasswordAction.OnResendClicked) },
            enabled = state.resendInSec == 0 && !state.isLoading,
        ) {
            Text(
                text = if (state.resendInSec > 0) "Ещё раз через ${state.resendInSec} с" else "Отправить код ещё раз",
                fontSize = 14.sp,
                color = if (state.resendInSec > 0) Gray700 else Blue800,
            )
        }
        TextButton(onClick = { onAction(ForgotPasswordAction.OnChangeEmailClicked) }) {
            Text(text = "Другой адрес", fontSize = 14.sp, color = Blue800)
        }
    }
}

@Composable
private fun MainButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .height(56.dp),
        onClick = onClick,
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gray800,
            disabledContainerColor = Gray800,
            contentColor = White,
            disabledContentColor = White,
        ),
        enabled = !isLoading,
    )
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Номер организации (9.2) — как на экране входа: запомненная организация или поле
@Composable
private fun OrgNumberBlock(state: ForgotPasswordState, onAction: (ForgotPasswordAction) -> Unit) {
    val remembered = state.rememberedOrgName
    if (remembered != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Организация", fontSize = 12.sp, color = Gray700)
                Text(
                    text = "$remembered · ${OrgNumber.format(state.orgNumber)}",
                    fontSize = 14.sp,
                    color = Gray800,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(onClick = { onAction(ForgotPasswordAction.OnChangeOrgClicked) }) {
                Text(text = "Сменить", fontSize = 14.sp, color = Blue800)
            }
        }
        return
    }
    LoginTextField(
        value = state.orgNumber,
        placeholderText = if (state.isOrgNumberRequired) "Номер организации" else "Номер организации (если знаете)",
        type = TextFieldType.NUMBER,
        onValueChange = { onAction(ForgotPasswordAction.InputOrgNumber(it)) }
    )
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp),
        text = when {
            state.isOrgNumberInvalid -> "Номер неверный — проверьте цифры"
            state.isOrgNumberRequired -> "Номер организации знает администратор"
            else -> "Не помните номер — введите почту вместо логина"
        },
        fontSize = 12.sp,
        color = if (state.isOrgNumberInvalid) Red500 else Gray700,
    )
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(modifier = Modifier, state = ForgotPasswordState(), onAction = {})
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordCodeStepPreview() {
    ForgotPasswordScreen(
        modifier = Modifier,
        state = ForgotPasswordState(step = ForgotPasswordStep.CODE, email = "ivanov@pushcha.by", resendInSec = 42),
        onAction = {}
    )
}
