package ed.maevski.hwwach.ui.navflow.startflow.splashscreen

data class SplashScreenState(
    val isLoading: Boolean = true,
    /** Серверы долго не отвечают — показать «будим сервер» */
    val isWakingServer: Boolean = false,
    /** Серверы не проснулись — показать ошибку и кнопку «Повторить» */
    val errorMessage: String? = null
)

sealed interface SplashScreenEvent {
    data object NavigateToAuth : SplashScreenEvent
    data object NavigateToMain : SplashScreenEvent
}

sealed interface SplashScreenAction {
    data object Retry : SplashScreenAction
}
