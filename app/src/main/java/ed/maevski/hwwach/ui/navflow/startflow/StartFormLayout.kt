package ed.maevski.hwwach.ui.navflow.startflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ed.maevski.hwwach.ui.theme.Gray250
import ed.maevski.hwwach.ui.theme.Gray400
import ed.maevski.hwwach.ui.theme.Gray700
import ed.maevski.hwwach.ui.theme.Gray800
import ed.maevski.hwwach.ui.theme.White

// Общий вид форм стартового потока (регистрация, регистрация организации).
// Фон — под статус-бар и панель навигации (edge-to-edge), содержимое — в безопасной области:
// safeDrawing учитывает статус-бар, кнопки навигации, вырез камеры и клавиатуру
@Composable
fun StartFormLayout(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
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
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Gray800
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = title,
                        fontSize = 22.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = subtitle,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            content()
        }
    }
}

// Расстояние между полями формы
@Composable
fun FormFieldSpacer() {
    Spacer(modifier = Modifier.height(20.dp))
}

// Пояснение над полями или под ними
@Composable
fun FormHint(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        color = Gray700,
        fontSize = 14.sp,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

// Заголовок группы полей («Администратор (вы)»)
@Composable
fun FormSectionTitle(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        color = Gray800,
        fontSize = 16.sp,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun FormPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .height(56.dp),
        onClick = onClick,
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gray800,
            disabledContainerColor = if (isLoading) Gray800 else Gray400,
            contentColor = White,
            disabledContentColor = if (isLoading) White else Gray250,
        ),
        enabled = enabled && !isLoading,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
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

@Composable
fun FormError(message: String) {
    if (message.isNotBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
