package ed.maevski.hwwach.ui.navflow.startflow.splashscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.usecases.user.CheckAuthTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val checkAuthTokenUseCase: CheckAuthTokenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashScreenState())
    val state: StateFlow<SplashScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SplashScreenEvent>()
    val events: SharedFlow<SplashScreenEvent> = _events.asSharedFlow()

    init {
        checkToken()
    }

    private fun checkToken() {
        viewModelScope.launch {
            // Небольшая задержка, чтобы экран не мелькал мгновенно и анимация успела проиграться
            delay(4000)
            
            val hasToken = checkAuthTokenUseCase()
            if (hasToken) {
                _events.emit(SplashScreenEvent.NavigateToMain)
            } else {
                _events.emit(SplashScreenEvent.NavigateToAuth)
            }
        }
    }

    fun handleAction(action: SplashScreenAction) {
        // No actions
    }
}
