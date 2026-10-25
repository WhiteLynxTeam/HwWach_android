package ed.maevski.hwwach.ui.navflow.mainflow.assetdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.data.local.dao.AssetPhotoCrossRefDao
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IAssetRolesRepository
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class AssetDetailScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assetRepository: IAssetRepository,
    private val photoRepository: IPhotoRepository,
    private val crossRefDao: AssetPhotoCrossRefDao,
    private val rolesRepository: IAssetRolesRepository,
    private val orgRepository: IOrgRepository,
    private val userProfileRepository: IUserProfileRepository,
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _state = MutableStateFlow(AssetDetailScreenState())
    val state: StateFlow<AssetDetailScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AssetDetailScreenEvent>()
    val events: SharedFlow<AssetDetailScreenEvent> = _events.asSharedFlow()

    private var canAssignAny = false
    private var serverInfoRequested = false

    init {
        loadAssetDetails()
        viewModelScope.launch {
            val me = userProfileRepository.userProfile.firstOrNull()
            canAssignAny = me?.can("asset_roles.assign") == true
            _state.update { it.copy(myPersonId = me?.personId) }
            (orgRepository.getPersons() as? DomainResult.Success)?.let { result ->
                _state.update { it.copy(persons = result.data.associateBy { p -> p.id }) }
            }
        }
    }

    private fun loadAssetDetails() {
        // Collect asset
        assetRepository.getAssetFlow(clientId).onEach { asset ->
            _state.update { it.copy(asset = asset, isLoading = false) }
            // ответственные и состояние — только на сервере; карточка ещё не синхронизирована — их нет
            val uuid = asset?.serverUuid
            if (uuid != null && !serverInfoRequested) {
                serverInfoRequested = true
                loadServerInfo(uuid)
            }
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

    private fun loadServerInfo(uuid: String) {
        viewModelScope.launch {
            when (val result = rolesRepository.getAssetInfo(uuid)) {
                is DomainResult.Success -> {
                    val info = result.data
                    // профиль читаем здесь же: init мог ещё не успеть его загрузить
                    val me = userProfileRepository.userProfile.firstOrNull()
                    canAssignAny = me?.can("asset_roles.assign") == true
                    val iAmMol = me?.personId != null && info.mol?.personId == me.personId
                    _state.update {
                        it.copy(serverInfo = info, myPersonId = me?.personId, canManageRoles = canAssignAny || iAmMol)
                    }
                }
                else -> Unit // офлайн — показываем только локальную карточку
            }
        }
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
            is AssetDetailScreenAction.OpenDialog -> {
                _state.update { it.copy(dialog = action.dialog, selectedPersonId = null, dialogText = "", infoMessage = "") }
                viewModelScope.launch {
                    // список людей мог устареть (новый МОЛ) — обновляем
                    (orgRepository.getPersons(force = true) as? DomainResult.Success)?.let { result ->
                        _state.update { it.copy(persons = result.data.associateBy { p -> p.id }) }
                    }
                }
            }
            AssetDetailScreenAction.CloseDialog -> _state.update { it.copy(dialog = null) }
            is AssetDetailScreenAction.SelectPerson -> _state.update { it.copy(selectedPersonId = action.personId) }
            is AssetDetailScreenAction.InputText -> _state.update { it.copy(dialogText = action.text) }
            AssetDetailScreenAction.ConfirmDialog -> confirmDialog()
        }
    }

    private fun confirmDialog() {
        val s = _state.value
        val uuid = s.asset?.serverUuid ?: return
        val dialog = s.dialog ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, infoMessage = "") }
            val result = when (dialog) {
                AssetDialog.TRANSFER_MOL -> s.selectedPersonId?.let { rolesRepository.transferMol(uuid, it, s.dialogText) }
                AssetDialog.ADD_USER -> s.selectedPersonId?.let { rolesRepository.addUser(uuid, it) }
                AssetDialog.REPORT_FAULT -> s.dialogText.takeIf { it.isNotBlank() }?.let { rolesRepository.reportFault(uuid, it) }
            }
            when (result) {
                null -> _state.update { it.copy(isSaving = false, infoMessage = if (dialog == AssetDialog.REPORT_FAULT) "Опишите неисправность" else "Выберите человека") }
                is DomainResult.Success -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            dialog = null,
                            infoMessage = when (dialog) {
                                AssetDialog.TRANSFER_MOL -> "Актив передан. Новый МОЛ подтвердит или откажется в приложении"
                                AssetDialog.ADD_USER -> "Назначено"
                                AssetDialog.REPORT_FAULT -> "Заявка на ремонт отправлена"
                            }
                        )
                    }
                    loadServerInfo(uuid)
                }
                is DomainResult.ValidationError -> _state.update { it.copy(isSaving = false, infoMessage = result.message) }
                else -> _state.update { it.copy(isSaving = false, infoMessage = "Не удалось сохранить, попробуйте позже") }
            }
        }
    }
}
