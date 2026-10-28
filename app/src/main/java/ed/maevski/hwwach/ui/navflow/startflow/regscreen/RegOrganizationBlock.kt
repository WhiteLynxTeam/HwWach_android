package ed.maevski.hwwach.ui.navflow.startflow.regscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.ui.navflow.startflow.LoginTextField
import ed.maevski.hwwach.ui.theme.Gray250
import ed.maevski.hwwach.ui.theme.Gray400
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800
import ed.maevski.hwwach.ui.theme.White

// Выбор организации на экране регистрации (9.1): код приглашения или поиск без подсказок
@Composable
fun RegOrganizationBlock(
    state: RegScreenState,
    onAction: (RegScreenAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Организация",
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeButton(
                modifier = Modifier.weight(1f),
                text = "Есть приглашение",
                selected = state.orgMode == OrgMode.INVITE,
                onClick = { onAction(RegScreenAction.SetOrgMode(OrgMode.INVITE)) }
            )
            ModeButton(
                modifier = Modifier.weight(1f),
                text = "Найти организацию",
                selected = state.orgMode == OrgMode.SEARCH,
                onClick = { onAction(RegScreenAction.SetOrgMode(OrgMode.SEARCH)) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (state.orgMode == OrgMode.INVITE) {
            LoginTextField(
                value = state.inviteCode,
                placeholderText = "Код приглашения, например K7M-42Q",
                onValueChange = { onAction(RegScreenAction.InputInviteCode(it)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val invite = state.invite
            if (invite != null) {
                Text(
                    text = "Организация: ${invite.organizationName}",
                    color = Gray800,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (state.inviteCode.isNotBlank()) {
                ActionButton(text = "Проверить код", loading = state.isCheckingInvite) {
                    onAction(RegScreenAction.OnCheckInviteClicked)
                }
            }
        } else {
            LoginTextField(
                value = state.orgQuery,
                placeholderText = "Название организации",
                onValueChange = { onAction(RegScreenAction.InputOrgQuery(it)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // кнопка появляется от 4 символов; запрос — только по нажатию
            if (state.canSearchOrg) {
                ActionButton(text = "Найти", loading = state.isSearchingOrg) {
                    onAction(RegScreenAction.OnSearchOrgClicked)
                }
            } else {
                Text(
                    text = "Введите не меньше ${RegScreenState.MIN_ORG_QUERY} букв названия",
                    color = Gray700,
                    fontSize = 13.sp
                )
            }
            state.orgResults?.forEach { org ->
                val selected = state.selectedOrg?.id == org.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { onAction(RegScreenAction.SelectOrg(org)) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) Gray800 else White,
                    border = BorderStroke(1.dp, if (selected) Gray800 else Gray400)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = org.name, color = if (selected) White else Gray800)
                        org.shortName?.takeIf { it.isNotBlank() && it != org.name }?.let {
                            Text(text = it, color = if (selected) Gray250 else Gray700, fontSize = 13.sp)
                        }
                    }
                }
            }
            if (state.selectedOrg != null) {
                Text(
                    text = "Заявку рассмотрит администратор организации",
                    color = Gray700,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (state.orgError.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.orgError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ModeButton(modifier: Modifier, text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(
            modifier = modifier.height(44.dp),
            onClick = onClick,
            shape = RoundedCornerShape(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gray800, contentColor = White)
        ) { Text(text = text, fontSize = 13.sp) }
    } else {
        OutlinedButton(
            modifier = modifier.height(44.dp),
            onClick = onClick,
            shape = RoundedCornerShape(36.dp)
        ) { Text(text = text, fontSize = 13.sp, color = Gray800) }
    }
}

@Composable
private fun ActionButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gray800, contentColor = White, disabledContainerColor = Gray800)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = text, fontSize = 15.sp)
        }
    }
}
