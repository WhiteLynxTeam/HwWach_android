package ed.maevski.hwwach

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Бэкенды на Render будит сплэш (SplashScreenViewModel): он ждёт их ответа и показывает,
// что сервер просыпается. Отдельный пинг при старте приложения дублировал бы его запросы.
@HiltAndroidApp
class HwWachApplication : Application()
