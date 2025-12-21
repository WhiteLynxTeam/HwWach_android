package com.whitelynxteam.hwwach.ui.navflow.startflow

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitelynxteam.hwwach.ui.theme.Grey300
import com.whitelynxteam.hwwach.ui.theme.Grey400
import com.whitelynxteam.hwwach.ui.theme.Grey800

@Composable
fun LoginTextField(
    value: String,
    placeholderText: String,
    onValueChange: (String) -> Unit,
) {
    val borderColor = if (value.isNotEmpty()) Grey800 else Grey300
    val textColor = Grey800

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(36.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            ),
            placeholder = {
                Text(
                    text = placeholderText,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey400
                )
            },
            singleLine = true
        )
    }
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
