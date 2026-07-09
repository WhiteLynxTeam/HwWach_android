package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.usecases.asset.GetAssetsUseCase
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
class AssetsScreenViewModel @Inject constructor(
    private val getAssetsUseCase: GetAssetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsScreenState())
    val state: StateFlow<AssetsScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AssetsScreenEvent>()
    val events: SharedFlow<AssetsScreenEvent> = _events.asSharedFlow()

    init {
        // Data loading is triggered by lifecycle ON_RESUME via LoadAssets action
    }

    private fun loadAssets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "")
            when (val result = getAssetsUseCase()) {
                is DomainResult.Success -> {
                    _state.value = _state.value.copy(
                        assets = result.data,
                        isLoading = false
                    )
                }
                is DomainResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is DomainResult.ServerError -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Server error: ${result.code}"
                    )
                }
                is DomainResult.UnauthorizedError -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Unauthorized"
                    )
                }
                is DomainResult.ValidationError -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun handleAction(action: AssetsScreenAction) {
        when (action) {
            is AssetsScreenAction.AddAssetClicked -> {
                viewModelScope.launch {
                    _events.emit(AssetsScreenEvent.NavigateToAddAsset)
                }
            }
            AssetsScreenAction.LoadAssets -> {
                loadAssets()
            }
            is AssetsScreenAction.AssetClicked -> {
                viewModelScope.launch {
                    _events.emit(AssetsScreenEvent.NavigateToAssetDetail(action.clientId))
                }
            }
        }
    }
}