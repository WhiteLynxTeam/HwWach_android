package ed.maevski.hwwach.ui.navflow.mainflow.camera

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.ui.navflow.mainflow.CameraResultProvider
import dagger.hilt.android.lifecycle.HiltViewModel
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
class CameraViewModel @Inject constructor(
    private val cameraResultProvider: CameraResultProvider
) : ViewModel() {

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>()
    val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

    fun handleAction(action: CameraAction) {
        when (action) {
            is CameraAction.OnPhotoCaptured -> {
                // Публикуем URI в Activity-scope SharedViewModel — все подписчики получат его напрямую
                cameraResultProvider.onPhotoCaptured(action.uri)
                viewModelScope.launch {
                    _events.emit(CameraEvent.NavigateBack)
                }
            }
            CameraAction.OnBackPressed -> {
                viewModelScope.launch {
                    _events.emit(CameraEvent.NavigateBack)
                }
            }
            CameraAction.ToggleLensFacing -> {
                _state.update {
                    val nextLens = if (it.lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                    it.copy(lensFacing = nextLens)
                }
            }
        }
    }
}
