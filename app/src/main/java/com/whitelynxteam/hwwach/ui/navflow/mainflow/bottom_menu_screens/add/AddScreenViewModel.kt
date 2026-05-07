package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum
import com.whitelynxteam.hwwach.domain.usecases.DeletePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetAllPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.SyncPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.SavePhotoUseCase
import com.whitelynxteam.hwwach.domain.usecases.SyncPendingPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.ResetStuckUploadsUseCase
import com.whitelynxteam.hwwach.domain.usecases.ResumeUploadedPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.RetrySyncFailedPhotosUseCase
import com.whitelynxteam.hwwach.domain.usecases.GetLastSyncTimeUseCase
import com.whitelynxteam.hwwach.domain.usecases.SaveLastSyncTimeUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddScreenViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase,
    private val getLastSyncTimeUseCase: GetLastSyncTimeUseCase,
    private val saveLastSyncTimeUseCase: SaveLastSyncTimeUseCase,
    private val syncPhotosUseCase: SyncPhotosUseCase,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val syncPendingPhotosUseCase: SyncPendingPhotosUseCase,
    private val resetStuckUploadsUseCase: ResetStuckUploadsUseCase,
    private val resumeUploadedPhotosUseCase: ResumeUploadedPhotosUseCase,
    private val retrySyncFailedPhotosUseCase: RetrySyncFailedPhotosUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddScreenState())
    val state: StateFlow<AddScreenState> = _state.asStateFlow()

    private var syncJob: Job? = null

    private val MIN_SYNC_INTERVAL_MS = 5 * 60 * 1000L // 5 минут

    private val _events = MutableSharedFlow<AddScreenEvent>()
    val events: SharedFlow<AddScreenEvent> = _events.asSharedFlow()

    init {
        getAllPhotosUseCase()
            .onEach { photos ->
                val hasPending = photos.any { it.status == PhotoUploadStatusEnum.PENDING }
                _state.update { it.copy(photos = photos, errorMessage = "", canUpload = hasPending) }
            }
            .catch { e ->
                _state.update { it.copy(errorMessage = "Ошибка загрузки фото: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Синхронизация с сервером — вызывать при каждом открытии экрана (LaunchedEffect).
     * Не вызывать из init повторно — только при первом создании ViewModel.
     * Rate limiting: минимум 5 минут между синхронизациями.
     */
    fun syncWithServer() {
        println("[SYNC] Called: isServerSyncing=${_state.value.isServerSyncing}")
        viewModelScope.launch {
            // Читаем время последней синхронизации через UseCase
            val lastSync = getLastSyncTimeUseCase().first()
            println("[SYNC] Called: isServerSyncing=${_state.value.isServerSyncing}, lastSyncTime=$lastSync")

            // Проверка на частые вызовы (rate limiting)
            val currentTime = System.currentTimeMillis()
            val timeSinceLastSync = currentTime - lastSync
            if (timeSinceLastSync < MIN_SYNC_INTERVAL_MS && lastSync > 0) {
                val remainingSeconds = (MIN_SYNC_INTERVAL_MS - timeSinceLastSync) / 1000
                println("[SYNC] Skipped: last sync was ${timeSinceLastSync / 1000}s ago, need to wait ${remainingSeconds}s more")
                return@launch
            }

            // Проверка, чтобы не запускать несколько синхронизаций одновременно
            if (_state.value.isServerSyncing) {
                println("[SYNC] Skipped: another sync is already in progress (isServerSyncing=true)")
                return@launch
            }

            _state.update { it.copy(isServerSyncing = true) }
            println("[SYNC] Starting sync operations...")
            try {
                // 1. Синхронизируем фотографии с бэкенда (которые были добавлены с других устройств)
                syncPhotosUseCase()
                println("[SYNC] Step 1: syncPhotosUseCase completed")
                // 2. Сбрасываем зависшие UPLOADING → FAILED
                resetStuckUploadsUseCase()
                // 3. Доотправляем подтверждение для UPLOADED
                resumeUploadedPhotosUseCase()
                // 4. Повторяем отправку FAILED фото
                retrySyncFailedPhotosUseCase()

                // Успех — сохраняем время последней синхронизации через UseCase
                val newSyncTime = System.currentTimeMillis()
                saveLastSyncTimeUseCase(newSyncTime)
                println("[SYNC] Completed successfully at ${newSyncTime}")
            } catch (e: Exception) {
                e.printStackTrace()
                println("[SYNC_ERROR] ${e.javaClass.simpleName}: ${e.message}")
                e.cause?.let { println("[SYNC_ERROR] Caused by: ${it.javaClass.simpleName}: ${it.message}") }
                _state.update { it.copy(errorMessage = "Ошибка синхронизации: ${e.message}") }
            } finally {
                _state.update { it.copy(isServerSyncing = false) }
            }
        }
    }

    fun handleAction(action: AddScreenAction) {
        when (action) {
            is AddScreenAction.SwitchMode -> {
                _state.update { it.copy(currentMode = action.mode) }
            }
            is AddScreenAction.InputName -> {
                _state.update { it.copy(name = action.value) }
            }
            is AddScreenAction.InputCategory -> {
                _state.update { it.copy(category = action.value) }
            }
            is AddScreenAction.InputInventoryNumber -> {
                _state.update { it.copy(inventoryNumber = action.value) }
            }
            is AddScreenAction.InputAddress -> {
                _state.update { it.copy(address = action.value) }
            }
            is AddScreenAction.InputComment -> {
                _state.update { it.copy(comment = action.value) }
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
            is AddScreenAction.OpenFullImage -> {
                viewModelScope.launch {
                    _events.emit(AddScreenEvent.NavigateToFullImage(action.clientId))
                }
            }
            is AddScreenAction.OnSubmitClicked -> {
                validateAndSubmit()
            }

            AddScreenAction.SyncPendingPhotos -> {
                syncJob = viewModelScope.launch {
                    _state.update { it.copy(isUploading = true, uploadError = "") }
                    try {
                        syncPendingPhotosUseCase()
                    } catch (_: CancellationException) {
                        // Cancelled by user
                    } catch (e: Exception) {
                        _state.update { it.copy(uploadError = "Ошибка загрузки: ${e.message}") }
                    }
                    _state.update { it.copy(isUploading = false) }
                }
            }

            AddScreenAction.CancelSync -> {
                syncJob?.cancel()
                syncJob = null
                _state.update { it.copy(isUploading = false) }
            }

            is AddScreenAction.FilterByStatus -> {
                _state.update { it.copy(statusFilter = action.status) }
            }

            // Действия ShowSourceSelector, OpenGallery и OpenCamera перехватываются локально в AddScreen.kt
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

            _events.emit(AddScreenEvent.ShowSuccessMessage)
            _events.emit(AddScreenEvent.NavigateBack)
        }
    }
}