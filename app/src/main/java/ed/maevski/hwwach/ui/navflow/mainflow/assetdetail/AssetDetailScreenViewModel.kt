package ed.maevski.hwwach.ui.navflow.mainflow.assetdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.data.local.dao.AssetPhotoCrossRefDao
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
class AssetDetailScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assetRepository: IAssetRepository,
    private val photoRepository: IPhotoRepository,
    private val crossRefDao: AssetPhotoCrossRefDao
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _state = MutableStateFlow(AssetDetailScreenState())
    val state: StateFlow<AssetDetailScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AssetDetailScreenEvent>()
    val events: SharedFlow<AssetDetailScreenEvent> = _events.asSharedFlow()

    init {
        loadAssetDetails()
    }

    private fun loadAssetDetails() {
        // Collect asset
        assetRepository.getAssetFlow(clientId).onEach { asset ->
            _state.update { it.copy(asset = asset, isLoading = false) }
        }.launchIn(viewModelScope)

        // Collect photos via cross references
        crossRefDao.getCrossRefsByAsset(clientId).onEach { crossRefs ->
            val photoClientIds = crossRefs.map { it.photoClientId }
            val photos = photoClientIds.mapNotNull { id ->
                photoRepository.getPhotoByClientId(id)
            }
            _state.update { it.copy(photos = photos) }
        }.launchIn(viewModelScope)
    }

    fun handleAction(action: AssetDetailScreenAction) {
        when (action) {
            AssetDetailScreenAction.OnBackPressed -> {
                viewModelScope.launch {
                    _events.emit(AssetDetailScreenEvent.NavigateBack)
                }
            }
            AssetDetailScreenAction.EditClicked -> {
                // TODO: emit navigate to edit
            }
            AssetDetailScreenAction.DeleteClicked -> {
                // TODO: emit show delete confirmation
            }
            is AssetDetailScreenAction.PhotoClicked -> {
                viewModelScope.launch {
                    _events.emit(AssetDetailScreenEvent.NavigateToFullImage(action.clientId))
                }
            }
        }
    }
}
