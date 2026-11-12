package ed.maevski.hwwach.ui.navflow.mainflow.addasset

import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.Category
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.MolMode
import ed.maevski.hwwach.domain.models.PersonBrief
import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.UploadQueueState

sealed class AddAssetScreenAction {
    data class InputName(val value: String) : AddAssetScreenAction()
    data class InputCategoryText(val value: String) : AddAssetScreenAction()
    data class SelectCategory(val category: Category) : AddAssetScreenAction()
    data object CreateCustomCategory : AddAssetScreenAction()
    data class SetCategoryDropdownVisible(val visible: Boolean) : AddAssetScreenAction()
    data class InputInventoryNumber(val value: String) : AddAssetScreenAction()
    data class InputComment(val value: String) : AddAssetScreenAction()

    data class AddImage(val uri: String) : AddAssetScreenAction()
    data class RemovePhoto(val photo: Photo) : AddAssetScreenAction()

    data object ShowSourceSelector : AddAssetScreenAction()
    data object OpenGallery : AddAssetScreenAction()
    data object OpenCamera : AddAssetScreenAction()

    // снимки, уже сделанные в приложении (вкладка «Галерея») и не прикреплённые к активу
    data object OpenAppPhotos : AddAssetScreenAction()
    data object CloseAppPhotos : AddAssetScreenAction()
    data class ToggleAppPhoto(val clientId: String) : AddAssetScreenAction()
    data object ConfirmAppPhotos : AddAssetScreenAction()

    // место: выбор в нижней панели
    data object OpenLocation : AddAssetScreenAction()
    data object CloseLocation : AddAssetScreenAction()
    data class LocationSetKind(val kind: AssetLocationKind) : AddAssetScreenAction()
    data class LocationQuery(val text: String) : AddAssetScreenAction()
    data class LocationPick(val location: Location) : AddAssetScreenAction()
    data class LocationAddNew(val name: String) : AddAssetScreenAction()
    data class LocationCutTo(val size: Int) : AddAssetScreenAction()
    data object LocationConfirm : AddAssetScreenAction()
    data object TogglePinLocation : AddAssetScreenAction()

    // МОЛ нового актива
    data object OpenMol : AddAssetScreenAction()
    data object CloseMol : AddAssetScreenAction()
    data class MolQuery(val text: String) : AddAssetScreenAction()
    data class MolPick(val mode: MolMode, val person: PersonBrief? = null) : AddAssetScreenAction()
    data object TogglePinMol : AddAssetScreenAction()

    data object RetryFailed : AddAssetScreenAction()
    data class OpenFailed(val clientId: String) : AddAssetScreenAction()

    data class OpenFullImage(val clientId: String) : AddAssetScreenAction()
    data object NavigateBack : AddAssetScreenAction()

    /** next = true — сохранить и сразу заводить следующий (форма очищается, закреплённое остаётся) */
    data class Submit(val next: Boolean) : AddAssetScreenAction()
}

sealed class AddAssetScreenEvent {
    data class ShowMessage(val message: String) : AddAssetScreenEvent()
    data class ShowErrorMessage(val message: String) : AddAssetScreenEvent()
    data object NavigateBack : AddAssetScreenEvent()
    data class NavigateToFullImage(val clientId: String) : AddAssetScreenEvent()
    data class NavigateToEdit(val clientId: String) : AddAssetScreenEvent()
}

// Нижняя панель выбора места: черновик, применяется кнопкой «Готово»
data class LocationSheetState(
    val kind: AssetLocationKind = AssetLocationKind.PLACE,
    val chain: List<Location> = emptyList(),
    val query: String = "",
)

data class AddAssetScreenState(
    // не null — правка существующего актива
    val editClientId: String? = null,
    // правка уже отправленного актива: сохраняется сразу на сервере
    val editSynced: Boolean = false,

    val name: String = "",
    val categoryText: String = "",
    val selectedCategory: Category? = null,
    val categorySuggestions: List<Category> = emptyList(),
    val isCategoryDropdownVisible: Boolean = false,
    val isSearchingCategories: Boolean = false,
    val inventoryNumber: String = "",
    val comment: String = "",
    val photos: List<Photo> = emptyList(),
    // снимки, сделанные в этой форме: убрать = удалить; выбранные из галереи только открепляются
    val createdPhotoIds: Set<String> = emptySet(),

    val appPhotosOpen: Boolean = false,
    val appPhotos: List<Photo> = emptyList(),
    val selectedAppPhotoIds: Set<String> = emptySet(),

    val locationKind: AssetLocationKind = AssetLocationKind.UNSET,
    val locationChain: List<Location> = emptyList(),
    val locationPinned: Boolean = false,
    val locationSheet: LocationSheetState? = null,
    val allLocations: List<Location> = emptyList(),
    val recentLocations: List<Location> = emptyList(),

    // выбрать МОЛ другого человека может только админ (asset_roles.assign)
    val canPickMol: Boolean = false,
    val molMode: MolMode = MolMode.DEFAULT,
    val molPerson: PersonBrief? = null,
    val molPinned: Boolean = false,
    val molSheetOpen: Boolean = false,
    val molQuery: String = "",
    val molCandidates: List<PersonBrief> = emptyList(),

    val queue: UploadQueueState = UploadQueueState(),
    val savedInSession: Int = 0,

    val isLoading: Boolean = false,
    val errorMessage: String = "",
) {
    val isEdit: Boolean get() = editClientId != null

    val canAddCustomCategory: Boolean
        get() {
            val trimmed = categoryText.trim()
            if (trimmed.length < 2) return false
            return categorySuggestions.none { it.name.equals(trimmed, ignoreCase = true) }
        }

    // одна строка в форме: «Каленковичи › Ферма 700 › Кабинет зав. фермы»
    val locationText: String
        get() = when (locationKind) {
            AssetLocationKind.PLACE -> locationChain.joinToString(" › ") { it.name }.ifEmpty { AssetLocationKind.UNSET.title }
            else -> locationKind.title
        }

    val molText: String
        get() = when (molMode) {
            MolMode.DEFAULT -> "По умолчанию — вы, если вы МОЛ"
            MolMode.NONE -> "Без МОЛ"
            MolMode.PERSON -> molPerson?.fullName ?: "—"
        }
}
