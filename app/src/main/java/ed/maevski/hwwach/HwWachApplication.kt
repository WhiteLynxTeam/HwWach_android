package ed.maevski.hwwach

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import ed.maevski.hwwach.domain.irepositories.IAssetUploadScheduler
import javax.inject.Inject

// Бэкенды на Render будит сплэш (SplashScreenViewModel): он ждёт их ответа и показывает,
// что сервер просыпается. Отдельный пинг при старте приложения дублировал бы его запросы.
@HiltAndroidApp
class HwWachApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var assetUploadScheduler: IAssetUploadScheduler

    // WorkManager создаёт воркеры через Hilt (автоинициализация отключена в манифесте)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // активы, оставшиеся в очереди с прошлого запуска (или после обновления приложения)
        assetUploadScheduler.schedule()
    }
}
