package ed.maevski.hwwach.ui.navflow.mainflow.fullimage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullImageScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val photoRepository: IPhotoRepository
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _state = MutableStateFlow(FullImageScreenState())
    val state: StateFlow<FullImageScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FullImageScreenEvent>()
    val events: SharedFlow<FullImageScreenEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val photo = photoRepository.getPhotoByClientId(clientId)
            _state.value = _state.value.copy(photo = photo)
        }
    }

    fun handleAction(action: FullImageScreenAction) {
        when (action) {
            FullImageScreenAction.OnBackPressed -> {
                viewModelScope.launch {
                    _events.emit(FullImageScreenEvent.NavigateBack)
                }
            }
        }
    }
}
