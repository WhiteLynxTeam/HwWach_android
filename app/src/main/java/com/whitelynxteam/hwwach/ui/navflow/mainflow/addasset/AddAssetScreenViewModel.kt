package com.whitelynxteam.hwwach.ui.navflow.mainflow.addasset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum
import com.whitelynxteam.hwwach.domain.usecases.DeletePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.SavePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddAssetScreenViewModel @Inject constructor(
    private val savePhotoUseCase: SavePhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssetScreenState())
    val state: StateFlow<AddAssetScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddAssetScreenEvent>()
    val events: SharedFlow<AddAssetScreenEvent> = _events.asSharedFlow()

    fun handleAction(action: AddAssetScreenAction) {
        when (action) {
            is AddAssetScreenAction.InputName -> {
                _state.update { it.copy(name = action.value) }
            }
            is AddAssetScreenAction.InputCategory -> {
                _state.update { it.copy(category = action.value) }
            }
            is AddAssetScreenAction.InputInventoryNumber -> {
                _state.update { it.copy(inventoryNumber = action.value) }
            }
            is AddAssetScreenAction.InputAddress -> {
                _state.update { it.copy(address = action.value) }
            }
            is AddAssetScreenAction.InputComment -> {
                _state.update { it.copy(comment = action.value) }
            }
            is AddAssetScreenAction.AddImage -> {
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
                    _state.update { it.copy(photos = it.photos + photo) }
                }
            }
            is AddAssetScreenAction.RemovePhoto -> {
                viewModelScope.launch {
                    deletePhotoUseCase(action.photo.clientId)
                    _state.update { state -> 
                        state.copy(photos = state.photos.filter { it.clientId != action.photo.clientId }) 
                    }
                }
            }
            is AddAssetScreenAction.OpenFullImage -> {
                viewModelScope.launch {
                    _events.emit(AddAssetScreenEvent.NavigateToFullImage(action.clientId))
                }
            }
            is AddAssetScreenAction.NavigateBack -> {
                viewModelScope.launch {
                    _events.emit(AddAssetScreenEvent.NavigateBack)
                }
            }

            // Действия ShowSourceSelector, OpenGallery и OpenCamera перехватываются локально в AddAssetScreen.kt
            // через localOnAction и никогда не доходят до ViewModel (требуют доступа к ActivityResultLaunchers)
            else -> { }
        }
    }

    private fun validateAndSubmit() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.name.isBlank()) {
                _state.update { it.copy(errorMessage = "Название обязательно") }
                return@launch
            }
            if (currentState.photos.isEmpty()) {
                _state.update { it.copy(errorMessage = "Добавьте хотя бы одно фото") }
                return@launch
            }

            _events.emit(AddAssetScreenEvent.ShowSuccessMessage)
            _events.emit(AddAssetScreenEvent.NavigateBack)
        }
    }
}