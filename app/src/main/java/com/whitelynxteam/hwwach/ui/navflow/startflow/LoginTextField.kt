package com.whitelynxteam.hwwach.ui.navflow.startflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitelynxteam.hwwach.ui.theme.Gray300
import com.whitelynxteam.hwwach.ui.theme.Gray400
import com.whitelynxteam.hwwach.ui.theme.Gray800
import com.whitelynxteam.hwwach.ui.theme.White

@Composable
fun LoginTextField(
    modifier: Modifier = Modifier,
    value: String,
    placeholderText: String,
    onValueChange: (String) -> Unit,
    type: TextFieldType = TextFieldType.TEXT,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Gray800
    )
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val borderColor = if (isFocused || value.isNotEmpty()) Gray800 else Gray300

    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation = when {
        type == TextFieldType.PASSWORD && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val keyboardOptions = when (type) {
        TextFieldType.PASSWORD -> KeyboardOptions(keyboardType = KeyboardType.Password)
        else -> KeyboardOptions.Default
    }

    val trailingIcon: @Composable (() -> Unit)? = when (type) {
        TextFieldType.PASSWORD -> {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                    )
                }
            }
        }
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(36.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(36.dp)
            )
            .padding(horizontal = 12.dp)
    ) {

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Gray800,
                unfocusedTextColor = Gray800
            ),
            textStyle = textStyle,
            placeholder = {
                Text(
                    text = placeholderText,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400
                )
            },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            singleLine = true
        )
    }
}


enum class TextFieldType {
    TEXT,
    PASSWORD
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
fun LoginTextFieldPreviewBoth() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Неактивное (пусто)", fontWeight = FontWeight.Bold)
        LoginTextField(
            value = "",
            placeholderText = "Логин",
            onValueChange = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Активное (заполнено)", fontWeight = FontWeight.Bold)
        LoginTextField(
            value = "user@example.com",
            placeholderText = "Логин",
            onValueChange = {}
        )
    }
}
