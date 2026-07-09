package ed.maevski.hwwach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ed.maevski.hwwach.ui.App
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            app.Render()
        }
    }
}