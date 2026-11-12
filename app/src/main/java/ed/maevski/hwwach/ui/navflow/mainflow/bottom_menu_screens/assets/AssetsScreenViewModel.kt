package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.usecases.asset.GetAssetsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetsScreenViewModel @Inject constructor(
    private val getAssetsUseCase: GetAssetsUseCase,
    assetRepository: IAssetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsScreenState())
    val state: StateFlow<AssetsScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AssetsScreenEvent>()
    val events: SharedFlow<AssetsScreenEvent> = _events.asSharedFlow()

    init {
        // список — из базы телефона: видны и не отправленные активы, и всё работает без сети.
        // Сервер только обновляет базу (LoadAssets на ON_RESUME)
        assetRepository.getAssetsFlow()
            .onEach { list ->
                _state.update { s -> s.copy(allAssets = list.sortedByDescending { it.createdAt ?: it.localCreatedAt }) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadAssets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.allAssets.isEmpty(), errorMessage = "") }
            val error = when (val result = getAssetsUseCase()) {
                is DomainResult.Success -> ""
                is DomainResult.NetworkError -> "Нет связи с сервером — показан список с телефона"
                is DomainResult.ServerError -> "Ошибка сервера (${result.code}) — показан список с телефона"
                is DomainResult.UnauthorizedError -> "Нужно войти заново"
                is DomainResult.ValidationError -> result.message
            }
            _state.update { it.copy(isLoading = false, errorMessage = error) }
        }
    }

    fun handleAction(action: AssetsScreenAction) {
        when (action) {
            is AssetsScreenAction.AddAssetClicked -> viewModelScope.launch {
                _events.emit(AssetsScreenEvent.NavigateToAddAsset)
            }
            AssetsScreenAction.LoadAssets -> loadAssets()
            is AssetsScreenAction.AssetClicked -> viewModelScope.launch {
                _events.emit(AssetsScreenEvent.NavigateToAssetDetail(action.clientId))
            }
            is AssetsScreenAction.Search -> _state.update { it.copy(query = action.query) }
        }
    }
}
