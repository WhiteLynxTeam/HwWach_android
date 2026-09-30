package ed.maevski.hwwach.ui.navflow.mainflow.addasset

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetStatusEnum
import ed.maevski.hwwach.domain.models.Category
import ed.maevski.hwwach.domain.models.ModerationStatusEnum
import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import ed.maevski.hwwach.domain.usecases.asset.AddAssetUseCase
import ed.maevski.hwwach.domain.usecases.category.SearchCategoriesUseCase
import ed.maevski.hwwach.domain.usecases.category.SyncCategoriesUseCase
import ed.maevski.hwwach.domain.usecases.photo.DeletePhotoUseCase
import ed.maevski.hwwach.domain.usecases.photo.SavePhotoUseCase
import ed.maevski.hwwach.ui.navflow.mainflow.CameraResultProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddAssetScreenViewModel @Inject constructor(
    private val savePhotoUseCase: SavePhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val addAssetUseCase: AddAssetUseCase,
    private val searchCategoriesUseCase: SearchCategoriesUseCase,
    private val syncCategoriesUseCase: SyncCategoriesUseCase,
    private val tokensRepository: ITokensRepository,
    private val cameraResultProvider: CameraResultProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssetScreenState())
    val state: StateFlow<AddAssetScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddAssetScreenEvent>()
    val events: SharedFlow<AddAssetScreenEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null
    private var currentUserUuid: String = ""

    init {
        // Извлекаем UUID текущего пользователя из JWT токена
        currentUserUuid = extractUserUuid(tokensRepository.accessTokenCache.value)

        // Подписываемся на результат камеры напрямую через Activity-scope SharedViewModel
        cameraResultProvider.photoUri
            .onEach { uri -> handleAction(AddAssetScreenAction.AddImage(uri)) }
            .launchIn(viewModelScope)

        // Запускаем фоновую синхронизацию категорий
        viewModelScope.launch {
            syncCategoriesUseCase()
            loadSuggestions("")
        }
    }

    fun handleAction(action: AddAssetScreenAction) {
        when (action) {
            is AddAssetScreenAction.InputName -> {
                _state.update { it.copy(name = action.value) }
            }

            is AddAssetScreenAction.InputCategoryText -> {
                val newText = action.value
                _state.update {
                    it.copy(
                        categoryText = newText,
                        selectedCategory = if (it.selectedCategory?.name == newText) it.selectedCategory else null,
                        isCategoryDropdownVisible = true
                    )
                }
                loadSuggestions(newText)
            }

            is AddAssetScreenAction.SelectCategory -> {
                _state.update {
                    it.copy(
                        selectedCategory = action.category,
                        categoryText = action.category.name,
                        isCategoryDropdownVisible = false
                    )
                }
            }

            is AddAssetScreenAction.CreateCustomCategory -> {
                val customText = _state.value.categoryText.trim()
                _state.update {
                    it.copy(
                        selectedCategory = null,
                        categoryText = customText,
                        isCategoryDropdownVisible = false
                    )
                }
            }

            is AddAssetScreenAction.SetCategoryDropdownVisible -> {
                _state.update { it.copy(isCategoryDropdownVisible = action.visible) }
                if (action.visible && _state.value.categorySuggestions.isEmpty()) {
                    loadSuggestions(_state.value.categoryText)
                }
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

            else -> {}
        }
    }

    private fun loadSuggestions(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val trimmed = query.trim()

            // 1. Если запрос пустой — загружаем популярные категории
            if (trimmed.isEmpty()) {
                searchCategoriesUseCase.getLocal("", currentUserUuid).collect { list ->
                    _state.update { it.copy(categorySuggestions = list, isSearchingCategories = false) }
                }
                return@launch
            }

            // 2. Если введено 1-2 символа — поиск не запускаем, чтобы избежать нерелевантного шума
            if (trimmed.length < 3) {
                _state.update { it.copy(categorySuggestions = emptyList(), isSearchingCategories = false) }
                return@launch
            }

            // 3. Если >= 3 символов — применяем debounce 300мс
            delay(300)
            _state.update { it.copy(isSearchingCategories = true) }

            // Локальный поиск в Room
            val localFlow = searchCategoriesUseCase.getLocal(trimmed, currentUserUuid)
            val currentLocal = localFlow.firstOrNull() ?: emptyList()
            _state.update { it.copy(categorySuggestions = currentLocal) }

            // Если локальных результатов мало (< 5), отправляем запрос на сервер для нечёткого pg_trgm поиска
            if (currentLocal.size < 5) {
                searchCategoriesUseCase.fetchRemote(trimmed)
            }

            // Подписываемся на обновления из Room (кэш пополнился удаленными результатами)
            localFlow.collect { updatedList ->
                _state.update { it.copy(categorySuggestions = updatedList, isSearchingCategories = false) }
            }
        }
    }

    private fun validateAndSubmit() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.name.isBlank()) {
                _state.update { it.copy(errorMessage = "Название обязательно") }
                return@launch
            }

            val categoryName = currentState.selectedCategory?.name
                ?: currentState.categoryText.trim().ifBlank { null }

            if (categoryName.isNullOrBlank()) {
                _state.update { it.copy(errorMessage = "Категория обязательна") }
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
                name = currentState.name.trim(),
                category = categoryName,
                categoryUuid = currentState.selectedCategory?.uuid,
                inventoryNum = currentState.inventoryNumber.trim().ifBlank { null },
                description = currentState.comment.trim().ifBlank { null },
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
                    delay(1000)
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

    private fun extractUserUuid(token: String?): String {
        if (token.isNullOrBlank()) return ""
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
                val json = JSONObject(payload)
                json.optString("user_uuid", json.optString("sub", ""))
            } else ""
        } catch (e: Exception) {
            ""
        }
    }
}