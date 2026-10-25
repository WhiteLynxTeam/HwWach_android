package ed.maevski.hwwach.ui.navflow.startflow.splashscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.usecases.settings.WakeUpServersUseCase
import ed.maevski.hwwach.domain.usecases.user.CheckAuthTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val checkAuthTokenUseCase: CheckAuthTokenUseCase,
    private val wakeUpServersUseCase: WakeUpServersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashScreenState())
    val state: StateFlow<SplashScreenState> = _state.asStateFlow()

    // replay = 1: событие навигации не теряется, если экран подписался позже
    private val _events = MutableSharedFlow<SplashScreenEvent>(replay = 1)
    val events: SharedFlow<SplashScreenEvent> = _events.asSharedFlow()

    private var job: Job? = null

    init {
        start()
    }

    fun handleAction(action: SplashScreenAction) {
        when (action) {
            SplashScreenAction.Retry -> start()
        }
    }

    private fun start() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _state.update { SplashScreenState() }
            val startedAt = System.currentTimeMillis()

            // Подсказка «будим сервер» — только если пробуждение затянулось
            val hint = launch {
                delay(WAKE_HINT_DELAY_MS)
                _state.update { it.copy(isWakingServer = true) }
            }
            val awake = wakeUpServersUseCase()
            hint.cancel()

            if (!awake) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isWakingServer = false,
                        errorMessage = "Сервер не отвечает.\nПроверьте подключение к интернету."
                    )
                }
                return@launch
            }

            // Тёплый сервер отвечает мгновенно — даём анимации проиграться, чтобы экран не мелькал
            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed < MIN_SPLASH_MS) delay(MIN_SPLASH_MS - elapsed)

            if (checkAuthTokenUseCase()) {
                _events.emit(SplashScreenEvent.NavigateToMain)
            } else {
                _events.emit(SplashScreenEvent.NavigateToAuth)
            }
        }
    }

    private companion object {
        const val WAKE_HINT_DELAY_MS = 3_000L
        const val MIN_SPLASH_MS = 1_500L
    }
}
