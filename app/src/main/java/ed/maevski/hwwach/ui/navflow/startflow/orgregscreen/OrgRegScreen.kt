package ed.maevski.hwwach.ui.navflow.startflow.orgregscreen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.ui.navflow.startflow.FormError
import ed.maevski.hwwach.ui.navflow.startflow.FormFieldSpacer
import ed.maevski.hwwach.ui.navflow.startflow.FormHint
import ed.maevski.hwwach.ui.navflow.startflow.FormPrimaryButton
import ed.maevski.hwwach.ui.navflow.startflow.FormSectionTitle
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.navflow.startflow.StartFormLayout
import ed.maevski.hwwach.ui.navflow.startflow.TextFieldType
import ed.maevski.hwwach.ui.theme.Blue800

// Тот же вид, что у «Регистрации» (StartFormLayout): заголовок, поля через 20dp, кнопка, ссылка на вход
@Composable
fun OrgRegScreen(
    modifier: Modifier = Modifier,
    state: OrgRegState,
    onAction: (OrgRegAction) -> Unit,
) {
    val input = { field: OrgRegField -> { value: String -> onAction(OrgRegAction.Input(field, value)) } }

    StartFormLayout(
        modifier = modifier,
        title = "Регистрация организации",
        subtitle = when (state.step) {
            OrgRegStep.EMAIL -> "Шаг 1 из 3: организация и почта"
            OrgRegStep.CODE -> "Шаг 2 из 3: код из письма"
            OrgRegStep.DETAILS -> "Шаг 3 из 3: данные организации и администратор"
        },
        onBack = { onAction(OrgRegAction.OnBackClicked) }
    ) {
        when (state.step) {
            OrgRegStep.EMAIL -> {
                FormHint("Регистрирующий становится администратором организации. Одна почта — одна организация.")
                FormFieldSpacer()
                LoginTextField(value = state.name, placeholderText = "Название организации", onValueChange = input(OrgRegField.NAME))
                FormFieldSpacer()
                LoginTextField(value = state.email, placeholderText = "Электронная почта", type = TextFieldType.LOGIN, onValueChange = input(OrgRegField.EMAIL))
            }

            OrgRegStep.CODE -> {
                if (state.infoMessage.isNotBlank()) {
                    FormHint(state.infoMessage)
                    FormFieldSpacer()
                }
                LoginTextField(value = state.code, placeholderText = "6 цифр", onValueChange = input(OrgRegField.CODE))
                TextButton(onClick = { onAction(OrgRegAction.OnResendClicked) }) {
                    Text(text = "Отправить код ещё раз", color = Blue800, fontSize = 14.sp)
                }
            }

            OrgRegStep.DETAILS -> {
                FormHint("${state.name} · ${state.email}")
                FormFieldSpacer()
                LoginTextField(value = state.shortName, placeholderText = "Короткое название (необязательно)", onValueChange = input(OrgRegField.SHORT_NAME))
                FormFieldSpacer()
                LoginTextField(value = state.address, placeholderText = "Адрес (необязательно)", onValueChange = input(OrgRegField.ADDRESS))
                FormFieldSpacer()
                LoginTextField(value = state.directorName, placeholderText = "Директор (необязательно)", onValueChange = input(OrgRegField.DIRECTOR))
                FormFieldSpacer()
                LoginTextField(value = state.ownerName, placeholderText = "Владелец (необязательно)", onValueChange = input(OrgRegField.OWNER))

                Spacer(modifier = Modifier.height(24.dp))
                FormSectionTitle("Администратор (вы)")
                FormFieldSpacer()
                LoginTextField(value = state.lastName, placeholderText = "Фамилия", onValueChange = input(OrgRegField.LAST_NAME))
                FormFieldSpacer()
                LoginTextField(value = state.firstName, placeholderText = "Имя", onValueChange = input(OrgRegField.FIRST_NAME))
                FormFieldSpacer()
                LoginTextField(value = state.middleName, placeholderText = "Отчество (необязательно)", onValueChange = input(OrgRegField.MIDDLE_NAME))
                FormFieldSpacer()
                LoginTextField(value = state.position, placeholderText = "Должность (необязательно)", onValueChange = input(OrgRegField.POSITION))
                FormFieldSpacer()
                LoginTextField(value = state.password, placeholderText = "Пароль", type = TextFieldType.PASSWORD, onValueChange = input(OrgRegField.PASSWORD))
                Spacer(modifier = Modifier.height(8.dp))
                FormHint("Логин — почта ${state.email}. Если за 14 дней в организации ничего не появится, она удалится.")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FormPrimaryButton(
            text = when (state.step) {
                OrgRegStep.EMAIL -> "Получить код"
                OrgRegStep.CODE -> "Подтвердить"
                OrgRegStep.DETAILS -> "Создать организацию"
            },
            isLoading = state.isLoading,
            onClick = { onAction(OrgRegAction.OnNextClicked) }
        )

        FormError(state.errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        // Ссылка на авторизацию — как на экране «Регистрация»
        TextButton(onClick = { onAction(OrgRegAction.OnAuthClicked) }) {
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
fun OrgRegScreenPreview() {
    OrgRegScreen(state = OrgRegState(), onAction = {})
}
