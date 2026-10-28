package ed.maevski.hwwach.ui.navflow.startflow.orgregscreen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.navflow.startflow.TextFieldType
import ed.maevski.hwwach.ui.theme.Blue800
import ed.maevski.hwwach.ui.theme.Gray250
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800
import ed.maevski.hwwach.ui.theme.White

@Composable
fun OrgRegScreen(
    modifier: Modifier = Modifier,
    state: OrgRegState,
    onAction: (OrgRegAction) -> Unit,
) {
    val input = { field: OrgRegField -> { value: String -> onAction(OrgRegAction.Input(field, value)) } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray250)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = { onAction(OrgRegAction.OnBackClicked) }
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Gray800)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Новая организация", fontSize = 22.sp, color = Gray800)
                    Text(
                        text = when (state.step) {
                            OrgRegStep.EMAIL -> "Шаг 1 из 3: почта организации"
                            OrgRegStep.CODE -> "Шаг 2 из 3: код из письма"
                            OrgRegStep.DETAILS -> "Шаг 3 из 3: организация и администратор"
                        },
                        fontSize = 14.sp,
                        color = Gray700
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (state.step) {
                OrgRegStep.EMAIL -> {
                    Text(
                        text = "Регистрирующий становится администратором организации. Одна почта — одна организация.",
                        color = Gray700,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    LoginTextField(value = state.email, placeholderText = "Электронная почта", type = TextFieldType.LOGIN, onValueChange = input(OrgRegField.EMAIL))
                }

                OrgRegStep.CODE -> {
                    if (state.infoMessage.isNotBlank()) {
                        Text(text = state.infoMessage, color = Gray700, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    LoginTextField(value = state.code, placeholderText = "6 цифр", onValueChange = input(OrgRegField.CODE))
                    TextButton(onClick = { onAction(OrgRegAction.OnResendClicked) }) {
                        Text(text = "Отправить код ещё раз", color = Blue800, fontSize = 14.sp)
                    }
                }

                OrgRegStep.DETAILS -> {
                    LoginTextField(value = state.name, placeholderText = "Название организации", onValueChange = input(OrgRegField.NAME))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.shortName, placeholderText = "Короткое название (необязательно)", onValueChange = input(OrgRegField.SHORT_NAME))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.address, placeholderText = "Адрес (необязательно)", onValueChange = input(OrgRegField.ADDRESS))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.directorName, placeholderText = "Директор (необязательно)", onValueChange = input(OrgRegField.DIRECTOR))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.ownerName, placeholderText = "Владелец (необязательно)", onValueChange = input(OrgRegField.OWNER))

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Администратор (вы)", color = Gray800, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.lastName, placeholderText = "Фамилия", onValueChange = input(OrgRegField.LAST_NAME))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.firstName, placeholderText = "Имя", onValueChange = input(OrgRegField.FIRST_NAME))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.middleName, placeholderText = "Отчество (необязательно)", onValueChange = input(OrgRegField.MIDDLE_NAME))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.position, placeholderText = "Должность (необязательно)", onValueChange = input(OrgRegField.POSITION))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginTextField(value = state.password, placeholderText = "Пароль", type = TextFieldType.PASSWORD, onValueChange = input(OrgRegField.PASSWORD))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Логин — почта ${state.email}. Если за 14 дней в организации ничего не появится, она удалится.",
                        color = Gray700,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = { onAction(OrgRegAction.OnNextClicked) },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gray800, contentColor = White, disabledContainerColor = Gray800)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(
                        text = when (state.step) {
                            OrgRegStep.EMAIL -> "Получить код"
                            OrgRegStep.CODE -> "Подтвердить"
                            OrgRegStep.DETAILS -> "Создать организацию"
                        },
                        fontSize = 16.sp
                    )
                }
            }

            if (state.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
