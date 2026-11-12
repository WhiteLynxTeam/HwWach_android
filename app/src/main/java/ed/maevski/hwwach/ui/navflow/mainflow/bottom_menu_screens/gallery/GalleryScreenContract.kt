package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.gallery

import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.LOCATION_PATH_SEP
import ed.maevski.hwwach.domain.models.PersonBrief
import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.PhotoGroup
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import ed.maevski.hwwach.ui.navflow.mainflow.addasset.LocationSheetState

// Что показывает галерея. Открытая группа ещё и принимает новые снимки
sealed class GalleryFilter {
    data object All : GalleryFilter()
    data object Ungrouped : GalleryFilter()
    data class Group(val id: String) : GalleryFilter()
}

sealed class GalleryScreenAction {
    data class AddImage(val uri: String) : GalleryScreenAction()
    data class RemovePhoto(val photo: Photo) : GalleryScreenAction()

    data object ShowSourceSelector : GalleryScreenAction()
    data object OpenGallery : GalleryScreenAction()
    data object OpenCamera : GalleryScreenAction()
    data object SyncPendingPhotos : GalleryScreenAction()
    data object CancelSync : GalleryScreenAction()

    data class FilterByStatus(val status: UploadStatusEnum?) : GalleryScreenAction()

    data class OpenFullImage(val clientId: String) : GalleryScreenAction()

    data object OnSubmitClicked : GalleryScreenAction()

    // ===== Разбор: фильтр по группам =====
    data class SelectFilter(val filter: GalleryFilter) : GalleryScreenAction()
    data object ToggleHideAttached : GalleryScreenAction()
    data object DismissNotice : GalleryScreenAction()

    // ===== Выбор снимков (долгое нажатие) =====
    data class PhotoClick(val clientId: String) : GalleryScreenAction()
    data class PhotoLongClick(val clientId: String) : GalleryScreenAction()
    data object ClearSelection : GalleryScreenAction()
    data object SelectAllVisible : GalleryScreenAction()
    data object DeleteSelected : GalleryScreenAction()
    data object CreateAssetFromSelected : GalleryScreenAction()

    // перенос выбранных в группу
    data object OpenMoveSheet : GalleryScreenAction()
    data object CloseMoveSheet : GalleryScreenAction()
    data class MoveSelectedTo(val groupId: String?) : GalleryScreenAction()
    data object MoveSelectedToNewGroup : GalleryScreenAction()

    // подпись выбранных снимков
    data object OpenNoteEditor : GalleryScreenAction()
    data object CloseNoteEditor : GalleryScreenAction()
    data class NoteInput(val text: String) : GalleryScreenAction()
    data object SaveNote : GalleryScreenAction()

    // ===== Редактор группы =====
    data object NewGroup : GalleryScreenAction()
    data class EditGroup(val id: String) : GalleryScreenAction()
    data object CloseGroupEditor : GalleryScreenAction()
    data class GroupName(val text: String) : GalleryScreenAction()
    data class GroupNote(val text: String) : GalleryScreenAction()
    data object GroupOpenPlace : GalleryScreenAction()
    data object GroupOpenPerson : GalleryScreenAction()
    data object GroupBack : GalleryScreenAction()
    data class GroupLocKind(val kind: AssetLocationKind) : GalleryScreenAction()
    data class GroupLocQuery(val text: String) : GalleryScreenAction()
    data class GroupLocPick(val location: Location) : GalleryScreenAction()
    data class GroupLocAddNew(val name: String) : GalleryScreenAction()
    data class GroupLocCutTo(val size: Int) : GalleryScreenAction()
    data object GroupLocConfirm : GalleryScreenAction()
    data object GroupLocClear : GalleryScreenAction()
    data class GroupPersonQuery(val text: String) : GalleryScreenAction()
    data class GroupPersonPick(val person: PersonBrief?) : GalleryScreenAction()
    data object SaveGroup : GalleryScreenAction()
    data object DeleteGroup : GalleryScreenAction()
}

sealed class GalleryScreenEvent {
    data object ShowSuccessMessage : GalleryScreenEvent()
    data class ShowErrorMessage(val message: String) : GalleryScreenEvent()
    data class NavigateToFullImage(val clientId: String) : GalleryScreenEvent()
    data object NavigateToAddAsset : GalleryScreenEvent()
}

enum class GroupEditorStep { MAIN, PLACE, PERSON }

data class GroupEditorState(
    val id: String? = null,           // null — новая группа
    val createdAt: Long = 0,
    val name: String = "",
    val note: String = "",
    val locationKind: AssetLocationKind = AssetLocationKind.UNSET,
    val chain: List<Location> = emptyList(),
    // путь сохранённой группы, если места уже нет в справочнике телефона
    val savedPath: String? = null,
    val person: PersonBrief? = null,
    val step: GroupEditorStep = GroupEditorStep.MAIN,
    val placeSheet: LocationSheetState = LocationSheetState(),
    val personQuery: String = "",
    // после сохранения перенести в группу выбранные снимки
    val moveSelected: Boolean = false,
    val error: String = "",
) {
    val placeText: String
        get() = when (locationKind) {
            AssetLocationKind.PLACE -> chain.joinToString(" › ") { it.name }
                .ifEmpty { savedPath?.replace(LOCATION_PATH_SEP, " › ") ?: "Не указано" }
            AssetLocationKind.UNSET -> "Не указано"
            else -> locationKind.title
        }
}

data class GroupChip(val group: PhotoGroup, val count: Int)

data class NoteEditorState(val photoIds: List<String>, val text: String)

data class GalleryScreenState(
    val photos: List<Photo> = emptyList(),
    // снимки после фильтра по группе и «скрыть в активах»
    val visiblePhotos: List<Photo> = emptyList(),

    val errorMessage: String = "",
    // сообщение после действия с выбранными (не сбрасывается обновлением списка)
    val notice: String = "",

    val isServerSyncing: Boolean = false, // Синхронизация с сервером при старте (reset/resume/retry)

    val isUploading: Boolean = false, // Загрузка фото на сервер (upload)
    val uploadError: String = "",

    val statusFilter: UploadStatusEnum? = null,

    val canUpload: Boolean = false, // Есть PENDING фото — показать кнопку загрузки

    val groups: List<GroupChip> = emptyList(),
    val ungroupedCount: Int = 0,
    val filter: GalleryFilter = GalleryFilter.All,
    val hideAttached: Boolean = false,
    val attachedIds: Set<String> = emptySet(),

    val selectedIds: Set<String> = emptySet(),
    val moveSheetOpen: Boolean = false,
    val noteEditor: NoteEditorState? = null,
    val groupEditor: GroupEditorState? = null,

    val allLocations: List<Location> = emptyList(),
    val recentLocations: List<Location> = emptyList(),
    val persons: List<PersonBrief> = emptyList(),
    val personsError: String = "",
) {
    val selectionMode: Boolean get() = selectedIds.isNotEmpty()

    val activeGroup: PhotoGroup?
        get() = (filter as? GalleryFilter.Group)?.let { f -> groups.firstOrNull { it.group.id == f.id }?.group }
}
