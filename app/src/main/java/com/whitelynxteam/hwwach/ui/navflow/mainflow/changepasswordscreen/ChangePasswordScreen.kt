package com.whitelynxteam.hwwach.ui.navflow.mainflow.changepasswordscreen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitelynxteam.hwwach.ui.navflow.startflow.LoginTextField
import com.whitelynxteam.hwwach.ui.navflow.startflow.TextFieldType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    state: ChangePasswordState,
    onAction: (ChangePasswordAction) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(text = "Смена пароля") },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ChangePasswordAction.OnBackPressed) }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                LoginTextField(
                    value = state.oldPassword,
                    placeholderText = "Текущий пароль",
                    onValueChange = { onAction(ChangePasswordAction.InputOldPassword(it)) },
                    type = TextFieldType.PASSWORD
                )
                Spacer(modifier = Modifier.height(16.dp))

                LoginTextField(
                    value = state.newPassword,
                    placeholderText = "Новый пароль",
                    onValueChange = { onAction(ChangePasswordAction.InputNewPassword(it)) },
                    type = TextFieldType.PASSWORD
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {
                        onAction(ChangePasswordAction.OnSubmitClicked)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
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
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(
                            text = "Сменить пароль",
                            fontSize = 16.sp
                        )
                    }
                }

                state.successMessage?.let { successMessage ->
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        text = successMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }

                state.errorMessage?.let { errorMessage ->
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(
        state = ChangePasswordState(),
        onAction = {}
    )
}
