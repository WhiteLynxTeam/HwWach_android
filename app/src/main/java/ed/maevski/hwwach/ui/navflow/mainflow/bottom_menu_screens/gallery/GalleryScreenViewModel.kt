package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.ILocationRepository
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoGroupRepository
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.PhotoGroup
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import ed.maevski.hwwach.domain.models.chainTo
import ed.maevski.hwwach.domain.usecases.photo.DeletePhotoUseCase
import ed.maevski.hwwach.domain.usecases.photo.GetAllPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.ResetStuckUploadsUseCase
import ed.maevski.hwwach.domain.usecases.photo.ResumeUploadedPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.RetrySyncFailedPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.SavePhotoUseCase
import ed.maevski.hwwach.domain.usecases.photo.SyncPendingPhotosUseCase
import ed.maevski.hwwach.domain.usecases.photo.SyncPhotosUseCase
import ed.maevski.hwwach.domain.usecases.settings.GetLastSyncTimeUseCase
import ed.maevski.hwwach.domain.usecases.settings.SaveLastSyncTimeUseCase
import ed.maevski.hwwach.ui.navflow.mainflow.AssetFormSeed
import ed.maevski.hwwach.ui.navflow.mainflow.CameraResultProvider
import ed.maevski.hwwach.ui.navflow.mainflow.addasset.LocationSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// снимки камеры, открытой из вкладки «Галерея»
private const val CAMERA_REQUESTER = "gallery"

@HiltViewModel
class GalleryScreenViewModel @Inject constructor(
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
    private val cameraResultProvider: CameraResultProvider,
    private val groupRepository: IPhotoGroupRepository,
    private val locationRepository: ILocationRepository,
    private val orgRepository: IOrgRepository,
    private val assetFormSeed: AssetFormSeed,
) : ViewModel() {

    private val _state = MutableStateFlow(GalleryScreenState())
    val state: StateFlow<GalleryScreenState> = _state.asStateFlow()

    private var syncJob: Job? = null

    private val MIN_SYNC_INTERVAL_MS = 5 * 60 * 1000L // 5 минут

    private val _events = MutableSharedFlow<GalleryScreenEvent>()
    val events: SharedFlow<GalleryScreenEvent> = _events.asSharedFlow()

    init {
        combine(
            getAllPhotosUseCase(),
            groupRepository.observeGroups(),
            groupRepository.observeAttachedPhotoIds(),
        ) { photos, groups, attached -> Triple(photos, groups, attached) }
            .onEach { (photos, groups, attached) ->
                val hasPending = photos.any { it.status == UploadStatusEnum.PENDING }
                val counts = photos.groupingBy { it.groupId }.eachCount()
                _state.update { s ->
                    // открытую группу удалили — возвращаемся ко всем снимкам
                    val filter = s.filter.let { f ->
                        if (f is GalleryFilter.Group && groups.none { it.id == f.id }) GalleryFilter.All else f
                    }
                    s.copy(
                        photos = photos,
                        errorMessage = "",
                        canUpload = hasPending,
                        groups = groups.map { GroupChip(it, counts[it.id] ?: 0) },
                        ungroupedCount = counts[null] ?: 0,
                        attachedIds = attached,
                        filter = filter,
                        // снимки, которых больше нет, из выбора убираем
                        selectedIds = s.selectedIds.filterTo(HashSet()) { id -> photos.any { it.clientId == id } },
                    ).withVisible()
                }
            }
            .catch { e ->
                _state.update { it.copy(errorMessage = "Ошибка загрузки фото: ${e.message}") }
            }
            .launchIn(viewModelScope)

        // открываем ту группу, что была открыта в прошлый раз
        viewModelScope.launch {
            val activeId = groupRepository.activeGroupId.first() ?: return@launch
            if (groupRepository.getGroup(activeId) != null) {
                _state.update { it.copy(filter = GalleryFilter.Group(activeId)).withVisible() }
            }
        }

        locationRepository.observeAll()
            .onEach { list -> _state.update { it.copy(allLocations = list) } }
            .launchIn(viewModelScope)
        locationRepository.observeRecent()
            .onEach { list -> _state.update { it.copy(recentLocations = list) } }
            .launchIn(viewModelScope)

        // Подписываемся на результат камеры напрямую через Activity-scope SharedViewModel
        cameraResultProvider.resultsFor(CAMERA_REQUESTER)
            .onEach { uri -> handleAction(GalleryScreenAction.AddImage(uri)) }
            .launchIn(viewModelScope)
    }

    private fun GalleryScreenState.withVisible(): GalleryScreenState {
        val list = photos
            .filter { p ->
                when (val f = filter) {
                    GalleryFilter.All -> true
                    GalleryFilter.Ungrouped -> p.groupId == null
                    is GalleryFilter.Group -> p.groupId == f.id
                }
            }
            .filter { !hideAttached || it.clientId !in attachedIds }
            .sortedBy { it.localCreatedAt }
        return copy(visiblePhotos = list)
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

    fun handleAction(action: GalleryScreenAction) {
        when (action) {
            is GalleryScreenAction.AddImage -> {
                // снимок попадает в открытую группу
                val groupId = (_state.value.filter as? GalleryFilter.Group)?.id
                viewModelScope.launch {
                    val photo = Photo(
                        clientId = UUID.randomUUID().toString(),
                        serverUuid = null,
                        localCreatedAt = System.currentTimeMillis(),
                        status = UploadStatusEnum.PENDING,
                        localPath = action.uri,
                        remoteUrl = null,
                        groupId = groupId,
                    )
                    try {
                        savePhotoUseCase(photo)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _state.update { it.copy(errorMessage = "Не удалось сохранить фото: ${e.message}") }
                    }
                }
            }
            is GalleryScreenAction.RemovePhoto -> {
                viewModelScope.launch {
                    deletePhotoUseCase(action.photo.clientId)
                }
            }
            is GalleryScreenAction.OpenFullImage -> {
                viewModelScope.launch {
                    _events.emit(GalleryScreenEvent.NavigateToFullImage(action.clientId))
                }
            }
            is GalleryScreenAction.OnSubmitClicked -> {
                validateAndSubmit()
            }

            GalleryScreenAction.SyncPendingPhotos -> {
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

            GalleryScreenAction.CancelSync -> {
                syncJob?.cancel()
                syncJob = null
                _state.update { it.copy(isUploading = false) }
            }

            is GalleryScreenAction.FilterByStatus -> {
                _state.update { it.copy(statusFilter = action.status) }
            }

            // камеру открывает экран (навигация), а здесь отмечаем, что снимки — для галереи
            GalleryScreenAction.OpenCamera -> cameraResultProvider.request(CAMERA_REQUESTER)

            // ===== Фильтр =====
            is GalleryScreenAction.SelectFilter -> {
                _state.update { it.copy(filter = action.filter, selectedIds = emptySet()).withVisible() }
                viewModelScope.launch { groupRepository.setActiveGroup((action.filter as? GalleryFilter.Group)?.id) }
            }
            GalleryScreenAction.DismissNotice -> _state.update { it.copy(notice = "") }
            GalleryScreenAction.ToggleHideAttached -> _state.update { it.copy(hideAttached = !it.hideAttached).withVisible() }

            // ===== Выбор =====
            is GalleryScreenAction.PhotoClick -> {
                if (_state.value.selectionMode) {
                    toggleSelected(action.clientId)
                } else {
                    viewModelScope.launch { _events.emit(GalleryScreenEvent.NavigateToFullImage(action.clientId)) }
                }
            }
            is GalleryScreenAction.PhotoLongClick -> toggleSelected(action.clientId)
            GalleryScreenAction.ClearSelection -> _state.update { it.copy(selectedIds = emptySet()) }
            GalleryScreenAction.SelectAllVisible -> _state.update { s ->
                val all = s.visiblePhotos.mapTo(HashSet()) { it.clientId }
                s.copy(selectedIds = if (s.selectedIds.containsAll(all)) emptySet() else all)
            }
            GalleryScreenAction.DeleteSelected -> deleteSelected()
            GalleryScreenAction.CreateAssetFromSelected -> createAssetFromSelected()

            GalleryScreenAction.OpenMoveSheet -> _state.update { it.copy(moveSheetOpen = true) }
            GalleryScreenAction.CloseMoveSheet -> _state.update { it.copy(moveSheetOpen = false) }
            is GalleryScreenAction.MoveSelectedTo -> {
                val ids = _state.value.selectedIds.toList()
                _state.update { it.copy(moveSheetOpen = false, selectedIds = emptySet()) }
                viewModelScope.launch { groupRepository.setPhotosGroup(ids, action.groupId) }
            }
            GalleryScreenAction.MoveSelectedToNewGroup -> {
                _state.update { it.copy(moveSheetOpen = false, groupEditor = GroupEditorState(moveSelected = true)) }
            }

            GalleryScreenAction.OpenNoteEditor -> _state.update { s ->
                val ids = s.visiblePhotos.map { it.clientId }.filter { it in s.selectedIds }
                // общая подпись выбранных — в поле; разные — поле пустое
                val notes = s.photos.filter { it.clientId in s.selectedIds }.map { it.note.orEmpty() }.distinct()
                s.copy(noteEditor = NoteEditorState(ids, if (notes.size == 1) notes[0] else ""))
            }
            GalleryScreenAction.CloseNoteEditor -> _state.update { it.copy(noteEditor = null) }
            is GalleryScreenAction.NoteInput -> _state.update { s -> s.copy(noteEditor = s.noteEditor?.copy(text = action.text)) }
            GalleryScreenAction.SaveNote -> {
                val editor = _state.value.noteEditor ?: return
                _state.update { it.copy(noteEditor = null, selectedIds = emptySet()) }
                viewModelScope.launch { groupRepository.setPhotosNote(editor.photoIds, editor.text) }
            }

            // ===== Группа =====
            GalleryScreenAction.NewGroup -> _state.update { it.copy(groupEditor = GroupEditorState()) }
            is GalleryScreenAction.EditGroup -> viewModelScope.launch { openGroupEditor(action.id) }
            GalleryScreenAction.CloseGroupEditor -> _state.update { it.copy(groupEditor = null) }
            is GalleryScreenAction.GroupName -> updateEditor { it.copy(name = action.text, error = "") }
            is GalleryScreenAction.GroupNote -> updateEditor { it.copy(note = action.text) }
            GalleryScreenAction.GroupOpenPlace -> updateEditor { e ->
                val kind = if (e.locationKind == AssetLocationKind.UNSET) AssetLocationKind.PLACE else e.locationKind
                e.copy(step = GroupEditorStep.PLACE, placeSheet = LocationSheetState(kind = kind, chain = e.chain))
            }
            GalleryScreenAction.GroupOpenPerson -> {
                updateEditor { it.copy(step = GroupEditorStep.PERSON, personQuery = "") }
                loadPersons()
            }
            GalleryScreenAction.GroupBack -> updateEditor { it.copy(step = GroupEditorStep.MAIN) }
            is GalleryScreenAction.GroupLocKind -> updatePlace { it.copy(kind = action.kind) }
            is GalleryScreenAction.GroupLocQuery -> updatePlace { it.copy(query = action.text) }
            is GalleryScreenAction.GroupLocPick -> {
                val chain = _state.value.allLocations.chainTo(action.location)
                updatePlace { it.copy(chain = chain, query = "") }
            }
            is GalleryScreenAction.GroupLocAddNew -> viewModelScope.launch {
                val sheet = _state.value.groupEditor?.placeSheet ?: return@launch
                val name = action.name.trim()
                if (name.isEmpty()) return@launch
                val created = locationRepository.createLocal(sheet.chain.lastOrNull(), name)
                updatePlace { it.copy(chain = it.chain + created, query = "") }
            }
            is GalleryScreenAction.GroupLocCutTo -> updatePlace { it.copy(chain = it.chain.take(action.size), query = "") }
            GalleryScreenAction.GroupLocConfirm -> updateEditor { e ->
                val sheet = e.placeSheet
                val kind = if (sheet.kind == AssetLocationKind.PLACE && sheet.chain.isEmpty()) AssetLocationKind.UNSET else sheet.kind
                e.copy(
                    locationKind = kind,
                    chain = if (kind == AssetLocationKind.PLACE) sheet.chain else emptyList(),
                    savedPath = null,
                    step = GroupEditorStep.MAIN,
                )
            }
            GalleryScreenAction.GroupLocClear -> updateEditor {
                it.copy(locationKind = AssetLocationKind.UNSET, chain = emptyList(), savedPath = null, step = GroupEditorStep.MAIN)
            }
            is GalleryScreenAction.GroupPersonQuery -> updateEditor { it.copy(personQuery = action.text) }
            is GalleryScreenAction.GroupPersonPick -> updateEditor { it.copy(person = action.person, step = GroupEditorStep.MAIN) }
            GalleryScreenAction.SaveGroup -> saveGroup()
            GalleryScreenAction.DeleteGroup -> {
                val id = _state.value.groupEditor?.id ?: return
                _state.update { it.copy(groupEditor = null) }
                viewModelScope.launch { groupRepository.deleteGroup(id) }
            }

            // Действия ShowSourceSelector и OpenGallery перехватываются локально в GalleryScreen.kt
            // через localOnAction и никогда не доходят до ViewModel (требуют доступа к ActivityResultLaunchers)
            else -> { }
        }
    }

    private fun toggleSelected(id: String) = _state.update { s ->
        s.copy(selectedIds = if (id in s.selectedIds) s.selectedIds - id else s.selectedIds + id)
    }

    private fun updateEditor(block: (GroupEditorState) -> GroupEditorState) {
        _state.update { s -> s.groupEditor?.let { s.copy(groupEditor = block(it)) } ?: s }
    }

    private fun updatePlace(block: (LocationSheetState) -> LocationSheetState) {
        updateEditor { it.copy(placeSheet = block(it.placeSheet)) }
    }

    private suspend fun openGroupEditor(id: String) {
        val g = groupRepository.getGroup(id) ?: return
        val chain = g.locationId?.let { locationRepository.chainFor(it) }.orEmpty()
        val person = g.personId?.let { pid ->
            ed.maevski.hwwach.domain.models.PersonBrief(pid, g.personName ?: "—", null, isMol = g.personIsMol, isActive = true)
        }
        _state.update {
            it.copy(
                groupEditor = GroupEditorState(
                    id = g.id,
                    createdAt = g.createdAt,
                    name = g.name,
                    note = g.note.orEmpty(),
                    locationKind = g.locationKind,
                    chain = chain,
                    savedPath = g.locationPath,
                    person = person,
                )
            )
        }
    }

    private fun loadPersons() {
        if (_state.value.persons.isNotEmpty()) return
        viewModelScope.launch {
            when (val r = orgRepository.getPersons()) {
                is DomainResult.Success -> _state.update { s ->
                    s.copy(persons = r.data.filter { it.isActive }.sortedBy { it.fullName }, personsError = "")
                }
                else -> _state.update { it.copy(personsError = "Список людей недоступен — нужна сеть и права администратора") }
            }
        }
    }

    private fun saveGroup() {
        val e = _state.value.groupEditor ?: return
        val name = e.name.trim()
        if (name.isEmpty()) {
            updateEditor { it.copy(error = "Назовите группу: место, человек или что снимаете") }
            return
        }
        val last = e.chain.lastOrNull()
        val group = PhotoGroup(
            id = e.id ?: UUID.randomUUID().toString(),
            name = name,
            note = e.note,
            locationKind = e.locationKind,
            locationId = if (e.locationKind == AssetLocationKind.PLACE) last?.id else null,
            // место не нашлось в справочнике телефона — путь остаётся прежним
            locationPath = if (e.locationKind == AssetLocationKind.PLACE) last?.path ?: e.savedPath else null,
            personId = e.person?.id,
            personName = e.person?.fullName,
            personIsMol = e.person?.isMol == true,
            createdAt = if (e.id == null) System.currentTimeMillis() else e.createdAt,
        )
        val moveIds = if (e.moveSelected) _state.value.selectedIds.toList() else emptyList()
        viewModelScope.launch {
            groupRepository.saveGroup(group)
            groupRepository.setPhotosGroup(moveIds, group.id)
            if (e.id == null) {
                // новую группу сразу открываем: следующие снимки пойдут в неё
                groupRepository.setActiveGroup(group.id)
                _state.update {
                    it.copy(groupEditor = null, selectedIds = emptySet(), filter = GalleryFilter.Group(group.id)).withVisible()
                }
            } else {
                _state.update { it.copy(groupEditor = null) }
            }
        }
    }

    private fun deleteSelected() {
        val s = _state.value
        val picked = s.photos.filter { it.clientId in s.selectedIds }
        // отправленные снимки удаляются на сервере (модерация), здесь — только свои неотправленные
        val deletable = picked.filter {
            it.status != UploadStatusEnum.UPLOADED && it.status != UploadStatusEnum.SYNCED && it.clientId !in s.attachedIds
        }
        _state.update { it.copy(selectedIds = emptySet()) }
        viewModelScope.launch {
            deletable.forEach { deletePhotoUseCase(it.clientId) }
            val kept = picked.size - deletable.size
            if (kept > 0) {
                _state.update {
                    it.copy(notice = "Не удалено: $kept — уже отправлены или прикреплены к активу")
                }
            }
        }
    }

    private fun createAssetFromSelected() {
        val s = _state.value
        val picked = s.visiblePhotos.filter { it.clientId in s.selectedIds }
        val free = picked.filter { it.clientId !in s.attachedIds }
        if (free.isEmpty()) {
            _state.update { it.copy(notice = "Выбранные снимки уже прикреплены к активам") }
            return
        }
        // группа: открытая, иначе общая у всех выбранных
        val groupId = (s.filter as? GalleryFilter.Group)?.id
            ?: free.map { it.groupId }.distinct().singleOrNull()
        assetFormSeed.put(free.map { it.clientId }, groupId)
        _state.update {
            it.copy(
                selectedIds = emptySet(),
                notice = if (free.size < picked.size) "Уже прикреплённые к активам снимки пропущены" else "",
            )
        }
        viewModelScope.launch { _events.emit(GalleryScreenEvent.NavigateToAddAsset) }
    }

    private fun validateAndSubmit() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.photos.isEmpty()) {
                _state.update { it.copy(errorMessage = "Добавьте хотя бы одно фото") }
                return@launch
            }

            _events.emit(GalleryScreenEvent.ShowSuccessMessage)
//            _events.emit(GalleryScreenEvent.NavigateBack)
        }
    }
}
