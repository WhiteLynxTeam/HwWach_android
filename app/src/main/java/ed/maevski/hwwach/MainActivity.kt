package ed.maevski.hwwach

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ed.maevski.hwwach.common.deeplink.PendingInvite
import ed.maevski.hwwach.ui.App
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PendingInvite.handle(intent?.data)
        setContent {
            app.Render()
        }
    }

    // ссылка-приглашение, когда приложение уже открыто
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        PendingInvite.handle(intent.data)
    }
}