package ed.maevski.hwwach.ui.navflow.mainflow.addasset

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.ILocationRepository
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoGroupRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import ed.maevski.hwwach.domain.irepositories.PinnedLocation
import ed.maevski.hwwach.domain.irepositories.PinnedMol
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.AssetStatusEnum
import ed.maevski.hwwach.domain.models.Category
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.ModerationStatusEnum
import ed.maevski.hwwach.domain.models.MolMode
import ed.maevski.hwwach.domain.models.PersonBrief
import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.PhotoGroup
import ed.maevski.hwwach.domain.models.chainTo
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import ed.maevski.hwwach.domain.usecases.category.SearchCategoriesUseCase
import ed.maevski.hwwach.domain.usecases.category.SyncCategoriesUseCase
import ed.maevski.hwwach.domain.usecases.photo.DeletePhotoUseCase
import ed.maevski.hwwach.domain.usecases.photo.SavePhotoUseCase
import ed.maevski.hwwach.ui.navflow.mainflow.AssetFormSeed
import ed.maevski.hwwach.ui.navflow.mainflow.CameraResultProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    savedStateHandle: SavedStateHandle,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val assetRepository: IAssetRepository,
    private val photoRepository: IPhotoRepository,
    private val locationRepository: ILocationRepository,
    private val orgRepository: IOrgRepository,
    private val userProfileRepository: IUserProfileRepository,
    private val searchCategoriesUseCase: SearchCategoriesUseCase,
    private val syncCategoriesUseCase: SyncCategoriesUseCase,
    private val tokensRepository: ITokensRepository,
    private val cameraResultProvider: CameraResultProvider,
    private val photoGroupRepository: IPhotoGroupRepository,
    assetFormSeed: AssetFormSeed,
) : ViewModel() {

    private val editClientId: String? = savedStateHandle.get<String>("editClientId")?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(AddAssetScreenState(editClientId = editClientId))
    val state: StateFlow<AddAssetScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddAssetScreenEvent>()
    val events: SharedFlow<AddAssetScreenEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null
    private var currentUserUuid: String = ""
    private var allPersons: List<PersonBrief> = emptyList()
    // исходная правка: для отправленного актива — какой МОЛ и статус были (их форма не меняет)
    private var editOriginal: Asset? = null
    // своя метка у каждой формы: открытая поверх другой (правка из очереди) не получит чужих снимков
    private val cameraRequester = "asset-form-" + UUID.randomUUID()
    // форма открыта из галереи: выбранные снимки и их группа
    private val seed = if (editClientId == null) assetFormSeed.consume() else null
    // группа снимков: её место и МОЛ остаются для следующих активов серии
    private var seedGroup: PhotoGroup? = null

    init {
        currentUserUuid = extractUserUuid(tokensRepository.accessTokenCache.value)

        cameraResultProvider.resultsFor(cameraRequester)
            .onEach { uri -> handleAction(AddAssetScreenAction.AddImage(uri)) }
            .launchIn(viewModelScope)

        locationRepository.observeAll()
            .onEach { list -> _state.update { it.copy(allLocations = list) } }
            .launchIn(viewModelScope)
        locationRepository.observeRecent()
            .onEach { list -> _state.update { it.copy(recentLocations = list) } }
            .launchIn(viewModelScope)
        assetRepository.observeUploadQueue()
            .onEach { q -> _state.update { it.copy(queue = q) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            syncCategoriesUseCase()
            loadSuggestions("")
        }
        viewModelScope.launch { locationRepository.refresh() }

        viewModelScope.launch {
            val me = userProfileRepository.userProfile.firstOrNull()
            val canPickMol = me?.can("asset_roles.assign") == true
            _state.update { it.copy(canPickMol = canPickMol) }
            if (editClientId != null) {
                loadForEdit(editClientId)
            } else {
                applyPinned()
                applySeed()
            }
            if (canPickMol && editClientId == null) {
                (orgRepository.getPersons() as? DomainResult.Success)?.let { r ->
                    allPersons = r.data.filter { it.isMol && it.isActive }
                    updateMolCandidates()
                }
            }
        }
    }

    // ===== Закреплённые место и МОЛ =====

    private suspend fun applyPinned() {
        val loc = locationRepository.pinnedLocation.first()
        val mol = locationRepository.pinnedMol.first()
        _state.update { s ->
            var n = s
            if (loc != null) n = n.copy(locationKind = loc.kind, locationChain = loc.chain, locationPinned = true)
            if (mol != null && s.canPickMol) {
                val person = mol.personId?.let { id -> PersonBrief(id, mol.personName ?: "—", null, isMol = true, isActive = true) }
                n = n.copy(molMode = mol.mode, molPerson = person, molPinned = true)
            }
            n
        }
    }

    // снимки из галереи; место и человек группы — поверх закреплённых
    private suspend fun applySeed() {
        val s = seed ?: return
        val photos = s.photoIds.mapNotNull { photoRepository.getPhotoByClientId(it) }
        val group = s.groupId?.let { photoGroupRepository.getGroup(it) }
        seedGroup = group
        val chain = if (group != null && group.locationKind == AssetLocationKind.PLACE) {
            group.locationId?.let { locationRepository.chainFor(it) }.orEmpty()
        } else {
            emptyList()
        }
        _state.update { st ->
            var n = st.copy(photos = st.photos + photos.filter { p -> st.photos.none { it.clientId == p.clientId } })
            if (group != null) {
                n = n.copy(groupName = group.name)
                when (group.locationKind) {
                    AssetLocationKind.PLACE -> {
                        if (chain.isNotEmpty()) n = n.copy(locationKind = AssetLocationKind.PLACE, locationChain = chain, locationPinned = false)
                    }
                    AssetLocationKind.UNSET -> Unit
                    else -> n = n.copy(locationKind = group.locationKind, locationChain = emptyList(), locationPinned = false)
                }
                if (st.canPickMol && group.personId != null && group.personIsMol) {
                    n = n.copy(
                        molMode = MolMode.PERSON,
                        molPerson = PersonBrief(group.personId, group.personName ?: "—", null, isMol = true, isActive = true),
                        molPinned = false,
                    )
                }
            }
            n
        }
    }

    private fun pinCurrentLocation() = viewModelScope.launch {
        val s = _state.value
        locationRepository.pinLocation(PinnedLocation(s.locationKind, s.locationChain))
    }

    private fun pinCurrentMol() = viewModelScope.launch {
        val s = _state.value
        locationRepository.pinMol(PinnedMol(s.molMode, s.molPerson?.id, s.molPerson?.fullName))
    }

    // ===== Правка =====

    private suspend fun loadForEdit(clientId: String) {
        val asset = assetRepository.getAsset(clientId)
        if (asset == null) {
            _state.update { it.copy(errorMessage = "Актив не найден на телефоне") }
            return
        }
        editOriginal = asset
        val photos = asset.photoClientIds.mapNotNull { photoRepository.getPhotoByClientId(it) }
        val chain = asset.locationId?.let { locationRepository.chainFor(it) }.orEmpty()
        _state.update {
            it.copy(
                editSynced = asset.serverUuid != null,
                name = asset.name,
                categoryText = asset.category.orEmpty(),
                selectedCategory = asset.categoryUuid?.let { uuid -> Category(uuid = uuid, name = asset.category.orEmpty(), level = 2) },
                inventoryNumber = asset.inventoryNum.orEmpty(),
                comment = asset.description.orEmpty(),
                photos = photos,
                // место из справочника не нашлось (не обновлён) — показываем «не указано», путь сохранится как был
                locationKind = if (asset.locationKind == AssetLocationKind.PLACE && chain.isEmpty()) AssetLocationKind.UNSET else asset.locationKind,
                locationChain = chain,
                molMode = asset.molMode,
            )
        }
    }

    fun handleAction(action: AddAssetScreenAction) {
        when (action) {
            is AddAssetScreenAction.InputName -> _state.update { it.copy(name = action.value) }

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

            is AddAssetScreenAction.SelectCategory -> _state.update {
                it.copy(selectedCategory = action.category, categoryText = action.category.name, isCategoryDropdownVisible = false)
            }

            is AddAssetScreenAction.CreateCustomCategory -> {
                val customText = _state.value.categoryText.trim()
                _state.update { it.copy(selectedCategory = null, categoryText = customText, isCategoryDropdownVisible = false) }
            }

            is AddAssetScreenAction.SetCategoryDropdownVisible -> {
                _state.update { it.copy(isCategoryDropdownVisible = action.visible) }
                if (action.visible && _state.value.categorySuggestions.isEmpty()) {
                    loadSuggestions(_state.value.categoryText)
                }
            }

            is AddAssetScreenAction.InputInventoryNumber -> _state.update { it.copy(inventoryNumber = action.value) }
            is AddAssetScreenAction.InputComment -> _state.update { it.copy(comment = action.value) }

            is AddAssetScreenAction.AddImage -> addImage(action.uri)

            is AddAssetScreenAction.RemovePhoto -> {
                val id = action.photo.clientId
                val createdHere = id in _state.value.createdPhotoIds
                _state.update { s -> s.copy(photos = s.photos.filter { it.clientId != id }, createdPhotoIds = s.createdPhotoIds - id) }
                // снимок из галереи приложения только открепляем — он остаётся в галерее
                if (createdHere) viewModelScope.launch { deletePhotoUseCase(id) }
            }

            // ===== Снимки приложения =====
            AddAssetScreenAction.OpenAppPhotos -> {
                _state.update { it.copy(appPhotosOpen = true, selectedAppPhotoIds = emptySet()) }
                viewModelScope.launch {
                    val inForm = _state.value.photos.map { it.clientId }.toSet()
                    val list = photoRepository.getUnattachedPhotosFlow().first().filter { it.clientId !in inForm }
                    val groups = photoGroupRepository.observeGroups().first()
                        .filter { g -> list.any { it.groupId == g.id } }
                    // сразу открыта группа серии, если в ней есть свободные снимки
                    val start = seedGroup?.id?.takeIf { id -> groups.any { it.id == id } }
                    _state.update { it.copy(appPhotos = list, appPhotoGroups = groups, appPhotoGroupFilter = start) }
                }
            }
            is AddAssetScreenAction.AppPhotosGroupFilter -> _state.update { it.copy(appPhotoGroupFilter = action.groupId) }
            AddAssetScreenAction.CloseAppPhotos -> _state.update { it.copy(appPhotosOpen = false) }
            is AddAssetScreenAction.ToggleAppPhoto -> _state.update {
                val sel = it.selectedAppPhotoIds
                it.copy(selectedAppPhotoIds = if (action.clientId in sel) sel - action.clientId else sel + action.clientId)
            }
            AddAssetScreenAction.ConfirmAppPhotos -> _state.update { s ->
                val picked = s.appPhotos.filter { it.clientId in s.selectedAppPhotoIds }
                s.copy(photos = s.photos + picked, appPhotosOpen = false, selectedAppPhotoIds = emptySet())
            }

            // ===== Место =====
            AddAssetScreenAction.OpenLocation -> _state.update {
                val kind = if (it.locationKind == AssetLocationKind.UNSET) AssetLocationKind.PLACE else it.locationKind
                it.copy(locationSheet = LocationSheetState(kind = kind, chain = it.locationChain))
            }
            AddAssetScreenAction.CloseLocation -> _state.update { it.copy(locationSheet = null) }
            is AddAssetScreenAction.LocationSetKind -> updateSheet { it.copy(kind = action.kind) }
            is AddAssetScreenAction.LocationQuery -> updateSheet { it.copy(query = action.text) }
            is AddAssetScreenAction.LocationPick -> {
                val chain = chainOf(action.location)
                updateSheet { it.copy(chain = chain, query = "") }
            }
            is AddAssetScreenAction.LocationAddNew -> viewModelScope.launch {
                val sheet = _state.value.locationSheet ?: return@launch
                val name = action.name.trim()
                if (name.isEmpty()) return@launch
                val created = locationRepository.createLocal(sheet.chain.lastOrNull(), name)
                updateSheet { it.copy(chain = it.chain + created, query = "") }
            }
            is AddAssetScreenAction.LocationCutTo -> updateSheet { it.copy(chain = it.chain.take(action.size), query = "") }
            AddAssetScreenAction.LocationConfirm -> {
                val sheet = _state.value.locationSheet ?: return
                val kind = if (sheet.kind == AssetLocationKind.PLACE && sheet.chain.isEmpty()) AssetLocationKind.UNSET else sheet.kind
                val chain = if (kind == AssetLocationKind.PLACE) sheet.chain else emptyList()
                _state.update { it.copy(locationKind = kind, locationChain = chain, locationSheet = null) }
                if (_state.value.locationPinned) pinCurrentLocation()
            }
            AddAssetScreenAction.TogglePinLocation -> {
                val pinned = !_state.value.locationPinned
                _state.update { it.copy(locationPinned = pinned) }
                if (pinned) pinCurrentLocation() else viewModelScope.launch { locationRepository.pinLocation(null) }
            }

            // ===== МОЛ =====
            AddAssetScreenAction.OpenMol -> {
                _state.update { it.copy(molSheetOpen = true, molQuery = "") }
                updateMolCandidates()
            }
            AddAssetScreenAction.CloseMol -> _state.update { it.copy(molSheetOpen = false) }
            is AddAssetScreenAction.MolQuery -> {
                _state.update { it.copy(molQuery = action.text) }
                updateMolCandidates()
            }
            is AddAssetScreenAction.MolPick -> {
                _state.update { it.copy(molMode = action.mode, molPerson = action.person, molSheetOpen = false) }
                if (_state.value.molPinned) pinCurrentMol()
            }
            AddAssetScreenAction.TogglePinMol -> {
                val pinned = !_state.value.molPinned
                _state.update { it.copy(molPinned = pinned) }
                if (pinned) pinCurrentMol() else viewModelScope.launch { locationRepository.pinMol(null) }
            }

            // ===== Очередь =====
            AddAssetScreenAction.RetryFailed -> viewModelScope.launch { assetRepository.retryFailed() }
            is AddAssetScreenAction.OpenFailed -> viewModelScope.launch {
                _events.emit(AddAssetScreenEvent.NavigateToEdit(action.clientId))
            }

            AddAssetScreenAction.OpenCamera -> cameraResultProvider.request(cameraRequester)

            is AddAssetScreenAction.OpenFullImage -> viewModelScope.launch {
                _events.emit(AddAssetScreenEvent.NavigateToFullImage(action.clientId))
            }
            is AddAssetScreenAction.NavigateBack -> viewModelScope.launch {
                _events.emit(AddAssetScreenEvent.NavigateBack)
            }
            is AddAssetScreenAction.Submit -> if (!_state.value.isLoading) submit(action.next)

            else -> {}
        }
    }

    private fun updateSheet(block: (LocationSheetState) -> LocationSheetState) {
        _state.update { s -> s.locationSheet?.let { s.copy(locationSheet = block(it)) } ?: s }
    }

    // цепочка от корня по справочнику в памяти
    private fun chainOf(location: Location): List<Location> = _state.value.allLocations.chainTo(location)

    private fun updateMolCandidates() {
        val q = _state.value.molQuery.trim().lowercase()
        val list = if (q.isEmpty()) allPersons else allPersons.filter { it.fullName.lowercase().contains(q) }
        _state.update { it.copy(molCandidates = list) }
    }

    private fun addImage(uri: String) {
        viewModelScope.launch {
            val photo = Photo(
                clientId = UUID.randomUUID().toString(),
                serverUuid = null,
                localCreatedAt = System.currentTimeMillis(),
                status = UploadStatusEnum.PENDING,
                localPath = uri,
                remoteUrl = null,
            )
            // Показываем сохранённую копию: исходный снимок камеры после сохранения удаляется
            val saved = try {
                savePhotoUseCase(photo)
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                _state.update { it.copy(errorMessage = "Не удалось сохранить фото: ${e.message}") }
                return@launch
            }
            _state.update { it.copy(photos = it.photos + saved, createdPhotoIds = it.createdPhotoIds + saved.clientId) }
        }
    }

    private fun submit(next: Boolean) {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Название обязательно") }
            return
        }
        val categoryName = s.selectedCategory?.name ?: s.categoryText.trim().ifBlank { null }
        if (categoryName.isNullOrBlank()) {
            _state.update { it.copy(errorMessage = "Категория обязательна") }
            return
        }
        if (s.photos.isEmpty()) {
            _state.update { it.copy(errorMessage = "Добавьте хотя бы одно фото") }
            return
        }
        val chain = if (s.locationKind == AssetLocationKind.PLACE) s.locationChain else emptyList()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = "") }
            val now = System.currentTimeMillis()
            val original = editOriginal
            val asset = if (original != null) {
                original.copy(
                    name = s.name.trim(),
                    category = categoryName,
                    categoryUuid = s.selectedCategory?.uuid,
                    inventoryNum = s.inventoryNumber.trim().ifBlank { null },
                    description = s.comment.trim().ifBlank { null },
                    photoClientIds = s.photos.map { it.clientId },
                    locationKind = s.locationKind,
                    locationId = chain.lastOrNull()?.id,
                    locationPath = chain.lastOrNull()?.path,
                    lastUpdatedLocally = now,
                )
            } else {
                Asset(
                    clientId = UUID.randomUUID().toString(),
                    serverUuid = null,
                    name = s.name.trim(),
                    category = categoryName,
                    categoryUuid = s.selectedCategory?.uuid,
                    inventoryNum = s.inventoryNumber.trim().ifBlank { null },
                    description = s.comment.trim().ifBlank { null },
                    assetStatus = AssetStatusEnum.ACTIVE,
                    moderationStatus = ModerationStatusEnum.PENDING,
                    status = UploadStatusEnum.PENDING,
                    adminComment = null,
                    createdAt = null,
                    updatedAt = null,
                    localCreatedAt = now,
                    lastUpdatedLocally = now,
                    photoClientIds = s.photos.map { it.clientId },
                    locationKind = s.locationKind,
                    locationId = chain.lastOrNull()?.id,
                    locationPath = chain.lastOrNull()?.path,
                    molMode = if (s.canPickMol) s.molMode else MolMode.DEFAULT,
                    molPersonId = if (s.canPickMol && s.molMode == MolMode.PERSON) s.molPerson?.id else null,
                )
            }

            val result = if (original != null) assetRepository.updateAsset(asset, chain) else assetRepository.addAsset(asset, chain)
            when (result) {
                is DomainResult.Success -> {
                    if (original == null && next) {
                        resetForNext()
                        _events.emit(AddAssetScreenEvent.ShowMessage("«${asset.name}» сохранён, отправится в фоне"))
                    } else {
                        _state.update { it.copy(isLoading = false) }
                        _events.emit(
                            AddAssetScreenEvent.ShowMessage(
                                when {
                                    original == null -> "«${asset.name}» сохранён, отправится в фоне"
                                    s.editSynced -> "Изменения сохранены"
                                    else -> "Изменения сохранены, актив отправится в фоне"
                                }
                            )
                        )
                        _events.emit(AddAssetScreenEvent.NavigateBack)
                    }
                }
                is DomainResult.ValidationError -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                is DomainResult.NetworkError -> _state.update {
                    it.copy(isLoading = false, errorMessage = "Нет связи с сервером: ${result.message}. Правка отправленного актива требует сети")
                }
                is DomainResult.UnauthorizedError -> _state.update { it.copy(isLoading = false, errorMessage = "Ошибка авторизации") }
                is DomainResult.ServerError -> _state.update { it.copy(isLoading = false, errorMessage = "Ошибка сервера (${result.code})") }
            }
        }
    }

    // следующий актив: закреплённое остаётся, остальное — как в новой форме
    private fun resetForNext() {
        val fromGroup = seedGroup != null
        _state.update { s ->
            val keepLocation = s.locationPinned || fromGroup
            val keepMol = s.molPinned || fromGroup
            AddAssetScreenState(
                categorySuggestions = s.categorySuggestions,
                allLocations = s.allLocations,
                recentLocations = s.recentLocations,
                queue = s.queue,
                canPickMol = s.canPickMol,
                molCandidates = s.molCandidates,
                savedInSession = s.savedInSession + 1,
                locationPinned = s.locationPinned,
                locationKind = if (keepLocation) s.locationKind else AssetLocationKind.UNSET,
                locationChain = if (keepLocation) s.locationChain else emptyList(),
                molPinned = s.molPinned,
                molMode = if (keepMol) s.molMode else MolMode.DEFAULT,
                molPerson = if (keepMol) s.molPerson else null,
                groupName = s.groupName,
            )
        }
        loadSuggestions("")
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

            val localFlow = searchCategoriesUseCase.getLocal(trimmed, currentUserUuid)
            val currentLocal = localFlow.firstOrNull() ?: emptyList()
            _state.update { it.copy(categorySuggestions = currentLocal) }

            // Если локальных результатов мало (< 5), отправляем запрос на сервер для нечёткого pg_trgm поиска
            if (currentLocal.size < 5) {
                searchCategoriesUseCase.fetchRemote(trimmed)
            }

            localFlow.collect { updatedList ->
                _state.update { it.copy(categorySuggestions = updatedList, isSearchingCategories = false) }
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
