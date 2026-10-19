package ed.maevski.hwwach

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ed.maevski.hwwach.domain.usecases.settings.WakeUpServersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HwWachApplication : Application() {

    @Inject
    lateinit var wakeUpServersUseCase: WakeUpServersUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Бэкенды на Render засыпают без трафика — будим их сразу при запуске,
        // пока идёт сплэш, чтобы первый вход не упирался в холодный старт
        appScope.launch { wakeUpServersUseCase() }
    }
}
