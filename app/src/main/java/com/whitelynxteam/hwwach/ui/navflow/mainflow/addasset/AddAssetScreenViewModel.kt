package com.whitelynxteam.hwwach.ui.navflow.mainflow.addasset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.models.Asset
import com.whitelynxteam.hwwach.domain.models.AssetStatusEnum
import com.whitelynxteam.hwwach.domain.models.ModerationStatusEnum
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.UploadStatusEnum
import com.whitelynxteam.hwwach.domain.usecases.asset.AddAssetUseCase
import com.whitelynxteam.hwwach.domain.usecases.photo.DeletePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.photo.SavePhotoUseCase
import com.whitelynxteam.hwwach.domain.DomainResult
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
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val addAssetUseCase: AddAssetUseCase,
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
                        status = UploadStatusEnum.PENDING,
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
            is AddAssetScreenAction.Submit -> {
                if (!_state.value.isLoading) {
                    validateAndSubmit()
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

            _state.update { it.copy(isLoading = true, errorMessage = "") }

            val asset = Asset(
                clientId = UUID.randomUUID().toString(),
                serverUuid = null,
                name = currentState.name,
                category = currentState.category?.displayName,
                inventoryNum = currentState.inventoryNumber,
                description = currentState.comment,
                assetStatus = AssetStatusEnum.ACTIVE,
                moderationStatus = ModerationStatusEnum.PENDING,
                status = UploadStatusEnum.PENDING,
                adminComment = null,
                createdAt = null,
                updatedAt = null,
                localCreatedAt = System.currentTimeMillis(),
                lastUpdatedLocally = System.currentTimeMillis(),
                photoClientIds = currentState.photos.map { it.clientId }
            )

            when (val result = addAssetUseCase(asset)) {
                is DomainResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(AddAssetScreenEvent.ShowSuccessMessage)
                    kotlinx.coroutines.delay(1000)
                    _events.emit(AddAssetScreenEvent.NavigateBack)
                }
                is DomainResult.NetworkError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is DomainResult.UnauthorizedError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = "Ошибка авторизации") }
                }
                else -> {
                    _state.update { it.copy(isLoading = false, errorMessage = "Неизвестная ошибка") }
                }
            }
        }
    }
}