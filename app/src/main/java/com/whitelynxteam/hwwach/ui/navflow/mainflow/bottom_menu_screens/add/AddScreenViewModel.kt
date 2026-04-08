package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum
import com.whitelynxteam.hwwach.domain.usecases.DeletePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetOrphanPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.SavePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.SyncPendingPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddScreenViewModel @Inject constructor(
    private val getOrphanPhotosUseCase: GetOrphanPhotosUseCase,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val syncPendingPhotosUseCase: SyncPendingPhotosUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddScreenState())
    val uiState: StateFlow<AddScreenState> = _uiState.asStateFlow()

    private var syncJob: Job? = null

    private val _events = MutableSharedFlow<AddScreenEvent>()
    val events: SharedFlow<AddScreenEvent> = _events.asSharedFlow()

    init {
        getOrphanPhotosUseCase()
            .onEach { photos ->
                _uiState.update { it.copy(photos = photos, errorMessage = "") }
            }
            .catch { e ->
                _uiState.update { it.copy(errorMessage = "Ошибка загрузки фото: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    fun handleAction(action: AddScreenAction) {
        when (action) {
            is AddScreenAction.SwitchMode -> {
                _uiState.update { it.copy(currentMode = action.mode) }
            }
            is AddScreenAction.InputName -> {
                _uiState.update { it.copy(name = action.value) }
            }
            is AddScreenAction.InputCategory -> {
                _uiState.update { it.copy(category = action.value) }
            }
            is AddScreenAction.InputInventoryNumber -> {
                _uiState.update { it.copy(inventoryNumber = action.value) }
            }
            is AddScreenAction.InputAddress -> {
                _uiState.update { it.copy(address = action.value) }
            }
            is AddScreenAction.InputComment -> {
                _uiState.update { it.copy(comment = action.value) }
            }
            is AddScreenAction.AddImage -> {
                viewModelScope.launch {
                    val photo = Photo(
                        clientId = UUID.randomUUID().toString(),
                        serverUuid = null,
                        localCreatedAt = System.currentTimeMillis(),
                        status = PhotoUploadStatusEnum.PENDING,
                        localPath = action.uri,
                        remoteUrl = null,
                    )
                    savePhotoUseCase(photo)
                }
            }
            is AddScreenAction.RemovePhoto -> {
                viewModelScope.launch {
                    deletePhotoUseCase(action.photo.clientId)
                }
            }
            is AddScreenAction.OnSubmitClicked -> {
                validateAndSubmit()
            }

            AddScreenAction.OpenImagePicker -> {
                viewModelScope.launch {
                    _events.emit(AddScreenEvent.OpenImagePicker)
                }
            }

            AddScreenAction.OpenCamera -> {
                viewModelScope.launch {
                    _events.emit(AddScreenEvent.OpenCamera)
                }
            }

            AddScreenAction.SyncPendingPhotos -> {
                syncJob = viewModelScope.launch {
                    _uiState.update { it.copy(isSyncing = true, syncError = "") }
                    try {
                        syncPendingPhotosUseCase()
                    } catch (_: CancellationException) {
                        // Cancelled by user
                    } catch (e: Exception) {
                        _uiState.update { it.copy(syncError = "Ошибка синхронизации: ${e.message}") }
                    }
                    _uiState.update { it.copy(isSyncing = false) }
                }
            }

            AddScreenAction.CancelSync -> {
                syncJob?.cancel()
                syncJob = null
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    private fun validateAndSubmit() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.name.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Название обязательно") }
                return@launch
            }
            if (currentState.photos.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Добавьте хотя бы одно фото") }
                return@launch
            }

            _events.emit(AddScreenEvent.ShowSuccessMessage)
            _events.emit(AddScreenEvent.NavigateBack)
        }
    }
}