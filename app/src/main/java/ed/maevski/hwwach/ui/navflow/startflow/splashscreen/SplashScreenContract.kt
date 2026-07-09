package ed.maevski.hwwach.ui.navflow.startflow.splashscreen

data class SplashScreenState(
    val isLoading: Boolean = true
)

sealed interface SplashScreenEvent {
    data object NavigateToAuth : SplashScreenEvent
    data object NavigateToMain : SplashScreenEvent
}

sealed interface SplashScreenAction {
    // No actions from user
}
