package ed.maevski.hwwach.ui.navflow.startflow.splashscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import ed.maevski.hwwach.R
import ed.maevski.hwwach.ui.theme.Gray300
import ed.maevski.hwwach.ui.theme.Gray500
import ed.maevski.hwwach.ui.theme.Gray600
import ed.maevski.hwwach.ui.theme.Gray800
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    state: SplashScreenState,
    onAction: (SplashScreenAction) -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim5_hwwach))

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Slogan
            Text(
                text = "ИНВЕНТАРИЗАЦИЯ • УЧЕТ • КОНТРОЛЬ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Gray800,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (state.isLoading) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic loading text
                var dotCount by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(500)
                        dotCount = (dotCount + 1) % 4
                    }
                }
                val loadingText = "загрузка данных" + ".".repeat(dotCount)

                Text(
                    text = loadingText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray500,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main author & lead developer block highlighted in a frame
            Surface(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Gray300),
                color = Color.Transparent,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Маевский Эдуард Аркадьевич",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Project Author & Lead Developer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray500,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Divider line
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(1.dp)
                    .background(Gray300)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Other contributors
            Text(
                text = "Design Concept: Надежда Никитина",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Gray600,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Assistant Developer: Юрис Мусин",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Gray600,
                textAlign = TextAlign.Center
            )
        }
    }
}
